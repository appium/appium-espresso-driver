plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

import io.appium.espressoserver.gradle.resolveCapabilityVersion
import io.appium.espressoserver.gradle.withCapabilityVersion
import io.appium.espressoserver.jvmtarget.AppiumJvmTarget
import org.gradle.api.GradleException
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val appiumCompileSdk: String by project
val appiumMinSdk: String by project
val appiumTargetSdk: String by project
val appiumBuildTools: String by project
val appiumSourceCompatibility: String by project
val appiumTargetCompatibility: String by project
val appiumJvmTarget: String by project

android {
    compileSdk = appiumCompileSdk.toInt()
    buildToolsVersion = appiumBuildTools
    namespace = "io.appium.espressoserver.lib"

    buildFeatures {
        buildConfig = true
    }

    flavorDimensions += "composeSupport"
    productFlavors {
        create("composeOn") {
            dimension = "composeSupport"
            isDefault = true
            buildConfigField("boolean", "COMPOSE_SUPPORT", "true")
        }
        create("composeOff") {
            dimension = "composeSupport"
            buildConfigField("boolean", "COMPOSE_SUPPORT", "false")
        }
    }

    defaultConfig {
        minSdk = appiumMinSdk.toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.valueOf(appiumSourceCompatibility.uppercase())
        targetCompatibility = JavaVersion.valueOf(appiumTargetCompatibility.uppercase())
    }

    packaging {
        resources.excludes.add("META-INF/**")
    }

    lint {
        targetSdk = appiumTargetSdk.toInt()
    }

    testOptions {
        targetSdk = appiumTargetSdk.toInt()
        unitTests.isReturnDefaultValues = true
    }

    publishing {
        singleVariant("composeOnRelease") {
            withSourcesJar()
        }
    }
}

kotlin {
    compilerOptions {
        val normalized = AppiumJvmTarget.resolveNormalized(appiumJvmTarget)
        jvmTarget.set(
            JvmTarget.entries.firstOrNull { it.target == normalized }
                ?: throw GradleException(
                    "Unsupported appiumJvmTarget \"$appiumJvmTarget\" (normalized \"$normalized\"). " +
                        "Use one of: ${JvmTarget.entries.joinToString { it.target }}",
                ),
        )
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components["composeOnRelease"])
            }
        }
    }
}

dependencies {
    // additionalAppDependencies placeholder (don't change or delete this line)

    api(libs.androidx.annotation) {
        withCapabilityVersion(project, "appiumAnnotationVersion", libs.versions.annotation.get())
    }
    api(libs.androidx.test.espresso.contrib) {
        withCapabilityVersion(project, "appiumEspressoVersion", libs.versions.espresso.get())
        // Exclude transitive dependencies to limit conflicts with AndroidX libraries from AUT.
        // Link to PR with fix and discussion https://github.com/appium/appium-espresso-driver/pull/596
        isTransitive = false
    }
    api(libs.androidx.test.espresso.web) {
        withCapabilityVersion(project, "appiumEspressoVersion", libs.versions.espresso.get())
        because("Espresso Web Atoms support (mobile: webAtoms)")
    }
    api(libs.androidx.test.uiautomator) {
        withCapabilityVersion(project, "appiumUiAutomatorVersion", libs.versions.uiautomator.get())
        because("UiAutomator support (mobile: uiautomator)")
    }
    api(libs.androidx.test.core) {
        withCapabilityVersion(project, "appiumAndroidxTestVersion", libs.versions.androidxTest.get())
    }
    api(libs.androidx.test.runner) {
        withCapabilityVersion(project, "appiumAndroidxTestVersion", libs.versions.androidxTest.get())
    }
    api(libs.androidx.test.rules) {
        withCapabilityVersion(project, "appiumAndroidxTestVersion", libs.versions.androidxTest.get())
    }
    api(libs.gson) {
        withCapabilityVersion(project, "appiumGsonVersion", libs.versions.gson.get())
    }
    api(libs.nanohttpd.webserver) {
        withCapabilityVersion(project, "appiumNanohttpdVersion", libs.versions.nanohttpd.get())
    }
    api(libs.kotlin.stdlib) {
        withCapabilityVersion(project, "appiumKotlin", libs.versions.kotlin.get())
    }
    api(libs.kotlin.reflect) {
        withCapabilityVersion(project, "appiumKotlin", libs.versions.kotlin.get())
    }
    "composeOnApi"(libs.androidx.compose.ui.test) {
        withCapabilityVersion(project, "appiumComposeVersion", libs.versions.composeUiTest.get())
        because("Android Compose support")
    }
    "composeOnApi"(libs.androidx.compose.ui.test.junit4) {
        withCapabilityVersion(project, "appiumComposeVersion", libs.versions.composeUiTest.get())
        because("Android Compose support")
    }

    testImplementation(libs.androidx.test.espresso.contrib) {
        withCapabilityVersion(project, "appiumEspressoVersion", libs.versions.espresso.get())
    }
    testImplementation(libs.junit) {
        withCapabilityVersion(project, "appiumJUnitVersion", libs.versions.junit.get())
    }
    testImplementation(libs.robolectric) {
        withCapabilityVersion(project, "appiumRobolectricVersion", libs.versions.robolectric.get())
    }
    testImplementation(libs.kotlin.test) {
        withCapabilityVersion(project, "appiumKotlin", libs.versions.kotlin.get())
    }
    testImplementation(libs.kotlin.test.junit) {
        withCapabilityVersion(project, "appiumKotlin", libs.versions.kotlin.get())
    }

    constraints {
        api(libs.kotlin.stdlib.jdk8) {
            version {
                require(resolveCapabilityVersion("appiumKotlin", libs.versions.kotlin.get()))
            }
        }
    }
}
