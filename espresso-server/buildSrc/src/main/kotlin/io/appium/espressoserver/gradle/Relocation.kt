package io.appium.espressoserver.gradle

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.TypePath
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
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

/** Bundled classes that extend a platform view (`android.view.*`, `android.widget.*`), directly or indirectly. */
internal fun viewTypes(classes: Map<String, ByteArray>): List<String> {
    val superNames = classes.mapValues { (_, bytes) -> ClassReader(bytes).superName }
    fun isView(name: String): Boolean {
        var current = superNames[name]
        while (current != null) {
            if (current.startsWith("android/view/") || current.startsWith("android/widget/")) {
                return true
            }
            current = superNames[current]
        }
        return false
    }
    return classes.keys.filter(::isView)
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

/** Members (`owner`, `name`, `descriptor`) that the code of [bytes] calls or reads, including method handles. */
internal fun memberReferences(bytes: ByteArray, onRef: (String, String, String) -> Unit) {
    ClassReader(bytes).accept(
        object : ClassVisitor(Opcodes.ASM9) {
            override fun visitMethod(
                access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?,
            ): MethodVisitor = object : MethodVisitor(Opcodes.ASM9) {
                override fun visitMethodInsn(
                    opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean,
                ) = onRef(owner, name, descriptor)

                override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) =
                    onRef(owner, name, descriptor)

                override fun visitInvokeDynamicInsn(name: String, descriptor: String, handle: Handle, vararg args: Any) {
                    (listOf(handle) + args).filterIsInstance<Handle>().forEach { onRef(it.owner, it.name, it.desc) }
                }
            }
        },
        ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES,
    )
}

/**
 * A relocated class lives in another package than the shared ones, so it cannot use a shared class that is
 * not public (e.g. extend a package-private base class), nor its package-private (or protected, unless it is
 * a subclass) members. Shares such classes, together with what they reference.
 */
internal fun shareNonPublicDependents(
    classes: Map<String, ByteArray>,
    closure: MutableSet<String>,
    isKept: (String) -> Boolean,
) {
    val references = HashMap<String, Set<String>>()
    val members = HashMap<String, Set<Triple<String, String, String>>>()
    val readers = HashMap<String, ClassReader>()
    fun reader(name: String) = readers.getOrPut(name) { ClassReader(classes.getValue(name)) }
    val isPublic = { name: String -> reader(name).access and Opcodes.ACC_PUBLIC != 0 }
    // Package-private and protected members declared by the class or inherited from the bundled superclasses
    val restricted = HashMap<String, Map<String, Boolean>>()
    fun restrictedMembers(owner: String): Map<String, Boolean> = restricted.getOrPut(owner) {
        val result = HashMap<String, Boolean>()
        var current: String? = owner
        while (current != null && current in classes) {
            val node = ClassNode().also { reader(current!!).accept(it, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG) }
            val declared = node.methods.map { Triple(it.name, it.desc, it.access) } +
                node.fields.map { Triple(it.name, it.desc, it.access) }
            declared
                .filter { (_, _, access) -> access and (Opcodes.ACC_PUBLIC or Opcodes.ACC_PRIVATE) == 0 }
                .forEach { (name, desc, access) ->
                    result.putIfAbsent(name + desc, access and Opcodes.ACC_PROTECTED != 0)
                }
            current = node.superName
        }
        result
    }
    fun isSubclass(name: String, owner: String): Boolean {
        var current: String? = name
        while (current != null && current in classes) {
            if (current == owner) {
                return true
            }
            current = reader(current).superName
        }
        return current == owner
    }
    var changed = true
    while (changed) {
        changed = false
        val nonPublic = closure.filterNotTo(HashSet(), isPublic)
        classes.keys.filter { it !in closure && !isKept(it) }.forEach { name ->
            val refs = references.getOrPut(name) {
                HashSet<String>().also { set -> referencedClasses(classes.getValue(name)) { set.add(it) } }
            }
            val memberRefs = members.getOrPut(name) {
                HashSet<Triple<String, String, String>>().also { set ->
                    memberReferences(classes.getValue(name)) { o, n, d -> set.add(Triple(o, n, d)) }
                }
            }
            val usesRestrictedMember = memberRefs.any { (owner, memberName, desc) ->
                owner in closure && restrictedMembers(owner)[memberName + desc]
                    ?.let { protected -> !protected || !isSubclass(name, owner) } == true
            }
            if (usesRestrictedMember || refs.any { it in nonPublic }) {
                growClosure(classes, closure, listOf(name))
                changed = true
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
    Files.move(tmp.toPath(), jar.toPath(), StandardCopyOption.REPLACE_EXISTING)
}
