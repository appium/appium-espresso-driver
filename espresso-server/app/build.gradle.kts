plugins {
    alias(libs.plugins.android.application)
}

import io.appium.espressoserver.gradle.resolveCapabilityVersion
import io.appium.espressoserver.jvmtarget.AppiumJvmTarget
import org.gradle.api.GradleException
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val appiumCompileSdk: String by project
val appiumMinSdk: String by project
val appiumTargetSdk: String by project
val appiumBuildTools: String by project
val appiumTargetPackage: String by project
val appiumSourceCompatibility: String by project
val appiumTargetCompatibility: String by project
val appiumJvmTarget: String by project

val useComposeLibrary: Boolean =
    (findProperty("appiumComposeSupport") as String?)?.equals("false", ignoreCase = true) != true

// Align androidTest dependency versions with :library when Espresso driver passes -PappiumJUnitVersion / -PappiumAndroidxTestVersion.
val junitVersion = resolveCapabilityVersion("appiumJUnitVersion", libs.versions.junit.get())
val androidxTestVersion = resolveCapabilityVersion("appiumAndroidxTestVersion", libs.versions.androidxTest.get())

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
    androidTestImplementation("junit:junit:$junitVersion")
    androidTestImplementation("androidx.test:core:$androidxTestVersion")
    androidTestImplementation("androidx.test:runner:$androidxTestVersion")

    // additionalAndroidTestDependencies placeholder (don't change or delete this line)
}
