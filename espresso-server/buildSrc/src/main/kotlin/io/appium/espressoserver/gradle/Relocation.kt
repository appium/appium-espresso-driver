package io.appium.espressoserver.gradle

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.TypePath
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

internal const val SERVICES_DIR = "META-INF/services/"
private val CLASS_NAME = Regex("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+\\.[A-Z][A-Za-z0-9_$]*")

/** Reads every `.class` of the given jars and directories into a map keyed by internal name. First one wins. */
internal fun readClasses(jars: Collection<File>, dirs: Collection<File>): Map<String, ByteArray> {
    val classes = LinkedHashMap<String, ByteArray>()
    jars.forEach { f ->
        JarFile(f).use { jar ->
            jar.entries().asSequence()
                .filter { it.name.endsWith(".class") && !it.name.startsWith("META-INF/") }
                .forEach { classes.putIfAbsent(it.name.removeSuffix(".class"), jar.getInputStream(it).readBytes()) }
        }
    }
    dirs.forEach { d ->
        d.walkTopDown().filter { it.isFile && it.name.endsWith(".class") }.forEach {
            classes.putIfAbsent(it.relativeTo(d).invariantSeparatorsPath.removeSuffix(".class"), it.readBytes())
        }
    }
    return classes
}

/** `META-INF/services` files of the given jars and directories: interface name to its provider class names. */
internal fun readServices(jars: Collection<File>, dirs: Collection<File>): Map<String, Set<String>> {
    val services = LinkedHashMap<String, LinkedHashSet<String>>()
    fun add(name: String, content: ByteArray) {
        content.toString(Charsets.UTF_8).lineSequence()
            .map { it.substringBefore('#').trim() }
            .filter { it.isNotEmpty() }
            .toCollection(services.getOrPut(name.removePrefix(SERVICES_DIR)) { LinkedHashSet() })
    }
    jars.forEach { f ->
        JarFile(f).use { jar ->
            jar.entries().asSequence()
                .filter { it.name.startsWith(SERVICES_DIR) && !it.isDirectory }
                .forEach { add(it.name, jar.getInputStream(it).readBytes()) }
        }
    }
    dirs.forEach { d ->
        d.resolve(SERVICES_DIR).takeIf { it.isDirectory }?.listFiles()?.filter { it.isFile }?.forEach {
            add(SERVICES_DIR + it.name, it.readBytes())
        }
    }
    return services
}

/**
 * Internal names of the classes [bytes] links against (signatures, code, constants).
 * Annotations are skipped: a missing annotation class is ignored at runtime, and following them
 * (e.g. `kotlin.Metadata`) would pull a whole library into the shared set.
 */
internal fun referencedClasses(bytes: ByteArray, onRef: (String) -> Unit) {
    val collector = object : Remapper() {
        override fun map(internalName: String): String {
            onRef(internalName)
            return internalName
        }
    }
    ClassReader(bytes).accept(AnnotationSkipper(ClassRemapper(ClassWriter(0), collector)), 0)
}

private class AnnotationSkipper(cv: ClassVisitor) : ClassVisitor(Opcodes.ASM9, cv) {
    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? = null
    override fun visitTypeAnnotation(
        typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean,
    ): AnnotationVisitor? = null

    override fun visitField(
        access: Int, name: String?, descriptor: String?, signature: String?, value: Any?,
    ): FieldVisitor? = super.visitField(access, name, descriptor, signature, value)?.let { fv ->
        object : FieldVisitor(Opcodes.ASM9, fv) {
            override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? = null
            override fun visitTypeAnnotation(
                typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean,
            ): AnnotationVisitor? = null
        }
    }

    override fun visitMethod(
        access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?,
    ): MethodVisitor? = super.visitMethod(access, name, descriptor, signature, exceptions)?.let { mv ->
        object : MethodVisitor(Opcodes.ASM9, mv) {
            override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? = null
            override fun visitAnnotationDefault(): AnnotationVisitor? = null
            override fun visitParameterAnnotation(
                parameter: Int, descriptor: String?, visible: Boolean,
            ): AnnotationVisitor? = null

            override fun visitTypeAnnotation(
                typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean,
            ): AnnotationVisitor? = null

            override fun visitInsnAnnotation(
                typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean,
            ): AnnotationVisitor? = null

            override fun visitTryCatchAnnotation(
                typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean,
            ): AnnotationVisitor? = null

            override fun visitLocalVariableAnnotation(
                typeRef: Int, typePath: TypePath?, start: Array<out Label>?, end: Array<out Label>?,
                index: IntArray?, descriptor: String?, visible: Boolean,
            ): AnnotationVisitor? = null
        }
    }
}

/**
 * Grows [closure] with every class reachable from [roots] through references.
 * A shared class shadows the AUT's copy, so whatever it references must stay shared too.
 */
internal fun growClosure(classes: Map<String, ByteArray>, closure: MutableSet<String>, roots: Collection<String>) {
    val queue = ArrayDeque<String>()
    roots.filter { it in classes && closure.add(it) }.forEach(queue::add)
    while (queue.isNotEmpty()) {
        referencedClasses(classes.getValue(queue.removeFirst())) { ref ->
            if (ref in classes && closure.add(ref)) {
                queue.add(ref)
            }
        }
    }
}

/** Moves the classes in [moved] under [prefix] (both slash-separated), including names in string constants. */
internal class PrefixRemapper(
    private val moved: Set<String>,
    private val prefix: String,
    private val bundled: Set<String> = emptySet(),
    private val onSuspicious: (String) -> Unit = {},
) : Remapper() {
    private val movedPackages: Set<String> by lazy { moved.map { it.substringBeforeLast('/', "") }.toSet() }

    override fun map(internalName: String): String = if (internalName in moved) prefix + internalName else internalName

    override fun mapValue(value: Any?): Any? {
        if (value is String) {
            // Class names in constants: reflection by name, Kotlin metadata (d2), descriptors
            val slashed = value.replace('.', '/')
            if (slashed in moved) {
                return if (value.contains('/')) prefix + value else (prefix + slashed).replace('/', '.')
            }
            if (value.startsWith("L") && value.endsWith(";") && value.substring(1, value.length - 1) in moved) {
                return "L$prefix${value.substring(1)}"
            }
            // Looks like a class of a relocated package that is not bundled: reflection we cannot follow
            if (CLASS_NAME.matches(value) && slashed !in bundled && slashed.substringBeforeLast('/') in movedPackages) {
                onSuspicious(value)
            }
        }
        return super.mapValue(value)
    }

    /** Relocated name of a class or service interface/provider given in dotted form. */
    fun mapDotted(name: String): String = map(name.replace('.', '/')).replace('/', '.')
}

/** Rewrites [jar] without the entries named in [names]. */
fun removeJarEntries(jar: File, names: Set<String>) {
    if (names.isEmpty()) {
        return
    }
    val tmp = File(jar.parentFile, "${jar.name}.tmp")
    JarFile(jar).use { src ->
        JarOutputStream(tmp.outputStream().buffered()).use { dst ->
            src.entries().asSequence().filter { it.name !in names }.forEach { e ->
                dst.putNextEntry(JarEntry(e.name).also { it.time = e.time })
                if (!e.isDirectory) {
                    src.getInputStream(e).copyTo(dst)
                }
                dst.closeEntry()
            }
        }
    }
    tmp.renameTo(jar)
}
