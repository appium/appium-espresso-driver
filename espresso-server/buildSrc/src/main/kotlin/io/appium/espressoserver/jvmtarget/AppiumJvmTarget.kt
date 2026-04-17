package io.appium.espressoserver.jvmtarget

import org.gradle.api.GradleException

/**
 * Normalizes and validates `appiumJvmTarget` / `-PappiumJvmTarget=…` from Gradle properties.
 *
 * Normalizes values (e.g. `1_8`, `8`, `VERSION_1_8`). Default `1.8` is the project baseline for emitted JVM
 * bytecode (Java 8 language level). Call sites map the returned string to [org.jetbrains.kotlin.gradle.dsl.JvmTarget].
 */
object AppiumJvmTarget {

    private fun normalizeAppiumJvmTarget(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return "1.8"
        }
        val withoutVersionPrefix = Regex("^VERSION_", RegexOption.IGNORE_CASE).replace(trimmed, "")
        val dotted = withoutVersionPrefix.replace('_', '.')
        return when (dotted) {
            "8" -> "1.8"
            else -> dotted
        }
    }

    /** Java bytecode level as a single integer (8 for 1.8, 11, 17, …). */
    private fun jvmBytecodeLevel(normalizedTarget: String): Int =
        when {
            normalizedTarget == "1.8" -> 8
            normalizedTarget.startsWith("1.") ->
                throw GradleException(
                    "Unsupported JVM target \"$normalizedTarget\". Use 1.8 (Java 8) for legacy 1.x bytecode.",
                )
            else ->
                normalizedTarget.toIntOrNull()
                    ?: throw GradleException(
                        "Invalid appiumJvmTarget \"$normalizedTarget\" (expected e.g. 1.8, 11, or 17).",
                    )
        }

    /**
     * Returns the normalized JVM target string (e.g. `1.8`, `17`) for Kotlin [org.jetbrains.kotlin.gradle.dsl.JvmTarget].
     */
    fun resolveNormalized(appiumJvmTargetProperty: String): String {
        val normalized = normalizeAppiumJvmTarget(appiumJvmTargetProperty)
        val level = jvmBytecodeLevel(normalized)
        if (level < 8) {
            throw GradleException(
                "Minimum supported appiumJvmTarget is 1.8 / Java 8 (bytecode level was $level from \"$appiumJvmTargetProperty\").",
            )
        }
        return normalized
    }
}
