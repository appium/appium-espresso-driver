package io.appium.espressoserver.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * Moves the bundled classes under [prefix] so they stop shadowing the AUT's copies (the server and the AUT
 * share one class loader). Kept and shared classes keep their names, see [keep] and [shared].
 *
 * `META-INF/services` files of relocated interfaces are rewritten into [servicesDir], and the originals
 * are listed in [excludedResources] (see [removeJarEntries]).
 */
abstract class RelocateClassesTask : DefaultTask() {
    // A pattern ending with a slash is a package prefix, otherwise it is a class with its inner classes
    private fun matches(name: String, pattern: String) =
        if (pattern.endsWith("/")) name.startsWith(pattern) else name == pattern || name.startsWith("$pattern$")


    @get:Classpath abstract val allJars: ListProperty<RegularFile>
    @get:Classpath abstract val allDirectories: ListProperty<Directory>

    /** Jars of the user declared dependencies: they and what they reference stay unrelocated. */
    @get:Classpath abstract val userJars: ConfigurableFileCollection

    /** Internal name prefix of the relocated classes, e.g. `io/appium/espressoserver/shaded/` */
    @get:Input abstract val prefix: Property<String>

    /** Internal name patterns that must keep their names (the platform or the manifest refer to them). */
    @get:Input abstract val keep: ListProperty<String>

    /** Internal name patterns shared with the AUT. Their reference closure is shared too. */
    @get:Input abstract val shared: ListProperty<String>

    /** Internal name patterns excluded from [shared]. */
    @get:Input abstract val sharedExclude: ListProperty<String>

    @get:OutputFile abstract val output: RegularFileProperty

    /** Java resources with the relocated service files (the root of `META-INF/services/...`). */
    @get:OutputDirectory abstract val servicesDir: DirectoryProperty

    /** Resource paths of the original service files that were rewritten, one per line. */
    @get:OutputFile abstract val excludedResources: RegularFileProperty

    @TaskAction
    fun run() {
        val jars = allJars.get().map { it.asFile }
        val dirs = allDirectories.get().map { it.asFile }
        val classes = readClasses(jars, dirs)
        val services = readServices(jars, dirs)
        val keeps = keep.get()
        val isKept = { n: String -> keeps.any { matches(n, it) } }

        val closure = HashSet<String>()
        val roots = classes.keys.filter { n ->
            shared.get().any { matches(n, it) } && sharedExclude.get().none { matches(n, it) }
        }
        logger.info("Shared roots: $roots")
        growClosure(classes, closure, roots)
        userJars.files.filter { it.name.endsWith(".jar") }.forEach { jar ->
            val before = closure.size
            growClosure(classes, closure, JarFile(jar).use { j ->
                j.entries().asSequence().map { it.name }
                    .filter { it.endsWith(".class") && !it.startsWith("META-INF/") }
                    .map { it.removeSuffix(".class") }.toList()
            })
            logger.lifecycle("User dependency ${jar.name} adds ${closure.size - before} shared classes")
        }
        // A service file keeps its name when its interface does, so its providers must keep theirs too
        do {
            val before = closure.size
            services.forEach { (iface, providers) ->
                val internal = iface.replace('.', '/')
                if (internal !in classes || internal in closure || isKept(internal)) {
                    logger.info("Service $iface keeps its name, sharing its providers")
                    growClosure(classes, closure, providers.map { it.replace('.', '/') }.filterNot(isKept))
                }
            }
        } while (closure.size != before)
        logger.lifecycle("Shared with the AUT: ${closure.size} classes")
        logger.info(
            "Shared packages: " + closure.groupingBy { it.split('/').take(2).joinToString("/") }.eachCount()
                .entries.sortedByDescending { it.value }.joinToString { "${it.key}=${it.value}" },
        )

        val pfx = prefix.get()
        val moved = classes.keys.filterTo(HashSet()) { n -> n != "module-info" && n !in closure && !isKept(n) }
        logger.lifecycle("Relocating ${moved.size} of ${classes.size} classes to $pfx")

        val suspicious = sortedSetOf<String>()
        val remapper = PrefixRemapper(moved, pfx, classes.keys, suspicious::add)
        output.get().asFile.outputStream().buffered().let(::JarOutputStream).use { jos ->
            classes.forEach { (name, bytes) ->
                val cw = ClassWriter(0)
                ClassReader(bytes).accept(ClassRemapper(cw, remapper), 0)
                jos.putNextEntry(JarEntry(remapper.map(name) + ".class"))
                jos.write(cw.toByteArray())
                jos.closeEntry()
            }
        }
        if (suspicious.isNotEmpty()) {
            logger.lifecycle(
                "Class names in string constants that are not bundled (reflection may not follow " +
                    "the relocation): ${suspicious.joinToString()}",
            )
        }

        val outDir = servicesDir.get().asFile.also { it.deleteRecursively() }
        val rewritten = services.filterKeys { it.replace('.', '/') in moved }
        rewritten.forEach { (iface, providers) ->
            val file = outDir.resolve(SERVICES_DIR + remapper.mapDotted(iface))
            file.parentFile.mkdirs()
            file.writeText(providers.joinToString("\n", postfix = "\n", transform = remapper::mapDotted))
        }
        excludedResources.get().asFile.writeText(rewritten.keys.joinToString("\n") { SERVICES_DIR + it })
        logger.lifecycle("Relocated service files: ${rewritten.keys.joinToString()}")
    }
}
