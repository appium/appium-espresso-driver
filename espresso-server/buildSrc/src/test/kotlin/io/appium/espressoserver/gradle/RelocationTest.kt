package io.appium.espressoserver.gradle

import org.junit.Assert.assertEquals
import org.junit.Test
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

class RelocationTest {
    private fun clazz(
        name: String,
        superName: String = "java/lang/Object",
        access: Int = Opcodes.ACC_PUBLIC,
        members: ClassWriter.() -> Unit = {},
    ): ByteArray = ClassWriter(0).apply {
        visit(Opcodes.V17, access, name, null, superName, null)
        members()
        visitEnd()
    }.toByteArray()

    private fun ClassWriter.staticMethod(name: String, access: Int, body: org.objectweb.asm.MethodVisitor.() -> Unit = {}) {
        visitMethod(access or Opcodes.ACC_STATIC, name, "()V", null, null).apply {
            visitCode()
            body()
            visitInsn(Opcodes.RETURN)
            visitMaxs(0, 0)
            visitEnd()
        }
    }

    private fun share(classes: Map<String, ByteArray>, roots: List<String>): Set<String> {
        val closure = HashSet<String>()
        growClosure(classes, closure, roots)
        shareNonPublicDependents(classes, closure) { false }
        return closure
    }

    @Test
    fun sharesCallersOfPackagePrivateMembersOfPublicSharedClass() {
        val classes = mapOf(
            "sample/Widget" to clazz("sample/Widget", "android/view/View") {
                staticMethod("touch", 0)
            },
            "sample/Peer" to clazz("sample/Peer") {
                staticMethod("call", Opcodes.ACC_PUBLIC) {
                    visitMethodInsn(Opcodes.INVOKESTATIC, "sample/Widget", "touch", "()V", false)
                }
            },
            "sample/Other" to clazz("sample/Other") {
                staticMethod("call", Opcodes.ACC_PUBLIC) {
                    visitMethodInsn(Opcodes.INVOKESTATIC, "sample/Widget", "publicOne", "()V", false)
                }
            },
        )
        assertEquals(setOf("sample/Widget", "sample/Peer"), share(classes, viewTypes(classes)))
    }

    @Test
    fun keepsCallersOfPublicMembersRelocated() {
        val classes = mapOf(
            "sample/Widget" to clazz("sample/Widget", "android/view/View") {
                staticMethod("touch", Opcodes.ACC_PUBLIC)
            },
            "sample/Peer" to clazz("sample/Peer") {
                staticMethod("call", Opcodes.ACC_PUBLIC) {
                    visitMethodInsn(Opcodes.INVOKESTATIC, "sample/Widget", "touch", "()V", false)
                }
            },
        )
        assertEquals(setOf("sample/Widget"), share(classes, viewTypes(classes)))
    }

    @Test
    fun allowsSubclassesToUseProtectedMembers() {
        val classes = mapOf(
            "sample/Widget" to clazz("sample/Widget", "android/view/View") {
                staticMethod("touch", Opcodes.ACC_PROTECTED)
            },
            "sample/Sub" to clazz("sample/Sub", "sample/Widget") {
                staticMethod("call", Opcodes.ACC_PUBLIC) {
                    visitMethodInsn(Opcodes.INVOKESTATIC, "sample/Widget", "touch", "()V", false)
                }
            },
            "sample/Peer" to clazz("sample/Peer") {
                staticMethod("call", Opcodes.ACC_PUBLIC) {
                    visitMethodInsn(Opcodes.INVOKESTATIC, "sample/Widget", "touch", "()V", false)
                }
            },
        )
        assertEquals(setOf("sample/Widget", "sample/Peer"), share(classes, listOf("sample/Widget")))
    }

    @Test
    fun sharesSubclassesCallingProtectedInstanceMembersOnOtherReceivers() {
        val classes = mapOf(
            "sample/Base" to clazz("sample/Base") {
                visitMethod(Opcodes.ACC_PROTECTED, "touch", "()V", null, null).apply {
                    visitCode()
                    visitInsn(Opcodes.RETURN)
                    visitMaxs(0, 0)
                    visitEnd()
                }
            },
            "sample/Sub" to clazz("sample/Sub", "sample/Base") {
                visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, "call", "(Lsample/Base;)V", null, null).apply {
                    visitCode()
                    visitVarInsn(Opcodes.ALOAD, 0)
                    visitMethodInsn(Opcodes.INVOKEVIRTUAL, "sample/Base", "touch", "()V", false)
                    visitInsn(Opcodes.RETURN)
                    visitMaxs(1, 1)
                    visitEnd()
                }
            },
        )
        assertEquals(setOf("sample/Base", "sample/Sub"), share(classes, listOf("sample/Base")))
    }

    private fun ClassWriter.constructor(access: Int, body: org.objectweb.asm.MethodVisitor.() -> Unit) {
        visitMethod(access, "<init>", "()V", null, null).apply {
            visitCode()
            body()
            visitInsn(Opcodes.RETURN)
            visitMaxs(2, 1)
            visitEnd()
        }
    }

    private fun baseWithProtectedConstructor() = clazz("sample/Base") {
        constructor(Opcodes.ACC_PROTECTED) {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        }
    }

    @Test
    fun sharesSubclassesThatConstructSeparateInstancesWithProtectedConstructors() {
        val classes = mapOf(
            "sample/Base" to baseWithProtectedConstructor(),
            "sample/Sub" to clazz("sample/Sub", "sample/Base") {
                staticMethod("run", Opcodes.ACC_PUBLIC) {
                    visitTypeInsn(Opcodes.NEW, "sample/Base")
                    visitInsn(Opcodes.DUP)
                    visitMethodInsn(Opcodes.INVOKESPECIAL, "sample/Base", "<init>", "()V", false)
                    visitInsn(Opcodes.POP)
                }
            },
        )
        assertEquals(setOf("sample/Base", "sample/Sub"), share(classes, listOf("sample/Base")))
    }

    @Test
    fun keepsSubclassesCallingSuperConstructorRelocated() {
        val classes = mapOf(
            "sample/Base" to baseWithProtectedConstructor(),
            "sample/Sub" to clazz("sample/Sub", "sample/Base") {
                constructor(Opcodes.ACC_PUBLIC) {
                    visitVarInsn(Opcodes.ALOAD, 0)
                    visitMethodInsn(Opcodes.INVOKESPECIAL, "sample/Base", "<init>", "()V", false)
                }
            },
        )
        assertEquals(setOf("sample/Base"), share(classes, listOf("sample/Base")))
    }

    @Test
    fun resolvesInheritedMembersReferencedThroughSubclass() {
        val classes = mapOf(
            "sample/Base" to clazz("sample/Base") { staticMethod("touch", 0) },
            "sample/Sub" to clazz("sample/Sub", "sample/Base") {
                staticMethod("run", Opcodes.ACC_PUBLIC) {
                    visitMethodInsn(Opcodes.INVOKESTATIC, "sample/Sub", "touch", "()V", false)
                }
            },
        )
        assertEquals(setOf("sample/Base", "sample/Sub"), share(classes, listOf("sample/Base")))
    }

    @Test
    fun sharesDependentsOfNonPublicSharedClasses() {
        val classes = mapOf(
            "sample/Base" to clazz("sample/Base", access = 0),
            "sample/Unrelated" to clazz("sample/Unrelated"),
            "sample/Child" to clazz("sample/Child", "sample/Base"),
        )
        assertEquals(setOf("sample/Base", "sample/Child"), share(classes, listOf("sample/Base")))
    }
}
