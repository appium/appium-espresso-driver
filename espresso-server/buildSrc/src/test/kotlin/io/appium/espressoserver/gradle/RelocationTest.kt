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
    fun sharesDependentsOfNonPublicSharedClasses() {
        val classes = mapOf(
            "sample/Base" to clazz("sample/Base", access = 0),
            "sample/Unrelated" to clazz("sample/Unrelated"),
            "sample/Child" to clazz("sample/Child", "sample/Base"),
        )
        assertEquals(setOf("sample/Base", "sample/Child"), share(classes, listOf("sample/Base")))
    }
}
