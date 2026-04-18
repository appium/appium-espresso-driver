package io.appium.espressoserver.gradle

import org.gradle.api.Project

/**
 * Resolves a dependency version from an optional Gradle property (e.g. `-PappiumKotlin=…` from the
 * Espresso driver's `toolsVersions` / `espressoBuildConfig`) with fallback to the version catalog default.
 */
fun Project.resolveCapabilityVersion(propertyName: String, catalogDefaultVersion: String): String {
    val fromCapability = findProperty(propertyName)?.toString()?.trim()
    return if (!fromCapability.isNullOrEmpty()) fromCapability else catalogDefaultVersion
}
