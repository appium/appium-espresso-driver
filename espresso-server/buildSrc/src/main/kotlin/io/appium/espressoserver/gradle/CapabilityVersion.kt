package io.appium.espressoserver.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency

/**
 * Resolves a dependency version from an optional Gradle property (e.g. `-PappiumKotlin=…` from the
 * Espresso driver's `toolsVersions` / `espressoBuildConfig`) with fallback to the version catalog default.
 */
fun Project.resolveCapabilityVersion(propertyName: String, catalogDefaultVersion: String): String {
    val fromCapability = findProperty(propertyName)?.toString()?.trim()
    return if (!fromCapability.isNullOrEmpty()) fromCapability else catalogDefaultVersion
}

/**
 * Applies a resolved capability version to a version-catalog dependency so `-Pappium*` overrides still work.
 */
fun ExternalModuleDependency.withCapabilityVersion(
    project: Project,
    propertyName: String,
    catalogDefaultVersion: String,
) {
    version { require(project.resolveCapabilityVersion(propertyName, catalogDefaultVersion)) }
}
