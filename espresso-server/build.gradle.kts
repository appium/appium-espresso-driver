// Top-level build file where you can add configuration options common to all sub-projects/modules.

// Overrides the Kotlin compiler AGP's built-in Kotlin support bundles, which otherwise lags
// behind the kotlin-stdlib/kotlin-reflect versions below and fails with metadata version errors.
buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}

allprojects {
    group = "io.appium.espressoserver"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
