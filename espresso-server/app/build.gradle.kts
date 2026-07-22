plugins {
    alias(libs.plugins.android.application)
}

import io.appium.espressoserver.gradle.withCapabilityVersion
import io.appium.espressoserver.jvmtarget.AppiumJvmTarget
import org.gradle.api.GradleException
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val appiumCompileSdk = project.property("appiumCompileSdk") as String
val appiumMinSdk = project.property("appiumMinSdk") as String
val appiumTargetSdk = project.property("appiumTargetSdk") as String
val appiumBuildTools = project.property("appiumBuildTools") as String
val appiumTargetPackage = project.property("appiumTargetPackage") as String
val appiumSourceCompatibility = project.property("appiumSourceCompatibility") as String
val appiumTargetCompatibility = project.property("appiumTargetCompatibility") as String
val appiumJvmTarget = project.property("appiumJvmTarget") as String

val useComposeLibrary: Boolean =
    (findProperty("appiumComposeSupport") as String?)?.equals("false", ignoreCase = true) != true

android {
    compileSdk = appiumCompileSdk.toInt()
    buildToolsVersion = appiumBuildTools
    namespace = "io.appium.espressoserver"

    defaultConfig {
        missingDimensionStrategy(
            "composeSupport",
            if (useComposeLibrary) {
                "composeOn"
            } else {
                "composeOff"
            },
        )
        // <instrumentation android:targetPackage=""/>
        applicationId = appiumTargetPackage
        // <manifest package=""/>
        testApplicationId = "io.appium.espressoserver.test"
        testHandleProfiling = false
        testFunctionalTest = false
        minSdk = appiumMinSdk.toInt()
        targetSdk = appiumTargetSdk.toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    signingConfigs {
        getByName("debug") {
            findProperty("appiumKeystoreFile")?.also {
                storeFile = file(it.toString())
            }

            findProperty("appiumKeystorePassword")?.also {
                storePassword = it.toString()
            }

            findProperty("appiumKeyAlias")?.also {
                keyAlias = it.toString()
            }

            findProperty("appiumKeyPassword")?.also {
                keyPassword = it.toString()
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.valueOf(appiumSourceCompatibility.uppercase())
        targetCompatibility = JavaVersion.valueOf(appiumTargetCompatibility.uppercase())
    }

    packaging {
        resources.excludes.add("META-INF/**")
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

dependencies {
    androidTestImplementation(project(":library"))
    androidTestImplementation(libs.junit) {
        withCapabilityVersion(project, "appiumJUnitVersion", libs.versions.junit.get())
    }
    androidTestImplementation(libs.androidx.test.core) {
        withCapabilityVersion(project, "appiumAndroidxTestVersion", libs.versions.androidxTest.get())
    }
    androidTestImplementation(libs.androidx.test.runner) {
        withCapabilityVersion(project, "appiumAndroidxTestVersion", libs.versions.androidxTest.get())
    }

    // additionalAndroidTestDependencies placeholder (don't change or delete this line)
}
