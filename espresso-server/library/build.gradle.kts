plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

import io.appium.espressoserver.gradle.resolveCapabilityVersion
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

// Versions: defaults from gradle/libs.versions.toml; -Pappium* overrides from Espresso driver (toolsVersions / capabilities).
val kotlinVersion = resolveCapabilityVersion("appiumKotlin", libs.versions.kotlin.get())
val annotationVersion = resolveCapabilityVersion("appiumAnnotationVersion", libs.versions.annotation.get())
val composeUiTestVersion =
    resolveCapabilityVersion("appiumComposeVersion", libs.versions.composeUiTest.get())
val gsonVersion = resolveCapabilityVersion("appiumGsonVersion", libs.versions.gson.get())
val espressoVersion = resolveCapabilityVersion("appiumEspressoVersion", libs.versions.espresso.get())
val nanohttpdVersion = resolveCapabilityVersion("appiumNanohttpdVersion", libs.versions.nanohttpd.get())
val androidxTestVersion = resolveCapabilityVersion("appiumAndroidxTestVersion", libs.versions.androidxTest.get())
val robolectricVersion = resolveCapabilityVersion("appiumRobolectricVersion", libs.versions.robolectric.get())
val junitVersion = resolveCapabilityVersion("appiumJUnitVersion", libs.versions.junit.get())
val uiautomatorVersion = resolveCapabilityVersion("appiumUiAutomatorVersion", libs.versions.uiautomator.get())

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

    api("androidx.annotation:annotation:$annotationVersion")
    api("androidx.test.espresso:espresso-contrib:$espressoVersion") {
        // Exclude transitive dependencies to limit conflicts with AndroidX libraries from AUT.
        // Link to PR with fix and discussion https://github.com/appium/appium-espresso-driver/pull/596
        isTransitive = false
    }
    api("androidx.test.espresso:espresso-web:$espressoVersion") {
        because("Espresso Web Atoms support (mobile: webAtoms)")
    }
    api("androidx.test.uiautomator:uiautomator:$uiautomatorVersion") {
        because("UiAutomator support (mobile: uiautomator)")
    }
    api("androidx.test:core:$androidxTestVersion")
    api("androidx.test:runner:$androidxTestVersion")
    api("androidx.test:rules:$androidxTestVersion")
    api("com.google.code.gson:gson:$gsonVersion")
    api("org.nanohttpd:nanohttpd-webserver:$nanohttpdVersion")
    api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    add("composeOnApi", "androidx.compose.ui:ui-test:$composeUiTestVersion") {
        because("Android Compose support")
    }
    add("composeOnApi", "androidx.compose.ui:ui-test-junit4:$composeUiTestVersion") {
        because("Android Compose support")
    }

    testImplementation("androidx.test.espresso:espresso-contrib:$espressoVersion")
    testImplementation("junit:junit:$junitVersion")
    testImplementation("org.robolectric:robolectric:$robolectricVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")

    constraints {
        api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    }
}
