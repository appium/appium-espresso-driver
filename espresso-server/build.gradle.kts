// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    val appiumKotlin =
        properties.getOrDefault("appiumKotlin", "1.5.10")

    val appiumAndroidGradlePlugin =
        properties.getOrDefault("appiumAndroidGradlePlugin", "7.0.3")

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$appiumKotlin")
        classpath("com.android.tools.build:gradle:$appiumAndroidGradlePlugin")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
