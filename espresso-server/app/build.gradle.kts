plugins {
    id("com.android.application")
    kotlin("android")
}

val appiumCompileSdk: String by project
val appiumMinSdk: String by project
val appiumTargetSdk: String by project
val appiumBuildTools: String by project
val appiumTargetPackage: String by project
val appiumSourceCompatibility: String by project
val appiumTargetCompatibility: String by project
val appiumJvmTarget: String by project
val appiumKotlin: String by project
val appiumAndroidxTestVersion: String by project
val appiumAnnotationVersion: String by project
val appiumComposeVersion: String by project
val appiumGsonVersion: String by project
val appiumEspressoVersion: String by project
val appiumMockitoVersion: String by project
val appiumNanohttpdVersion: String by project
val appiumRobolectricVersion: String by project
val appiumJUnitVersion: String by project
val appiumUiAutomatorVersion: String by project

android {
    compileSdk = appiumCompileSdk.toInt()
    buildToolsVersion = appiumBuildTools
    namespace = "io.appium.espressoserver"

    defaultConfig {
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
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
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

    kotlinOptions {
        jvmTarget = appiumJvmTarget
    }

    packaging {
        resources.excludes.add("META-INF/**")
    }
}

dependencies {
    androidTestImplementation(project(":library"))
    androidTestImplementation("junit:junit:$appiumJUnitVersion")
    androidTestImplementation("androidx.test:core:$appiumAndroidxTestVersion")
    androidTestImplementation("androidx.test:runner:$appiumAndroidxTestVersion")

    // additionalAndroidTestDependencies placeholder (don't change or delete this line)
}
