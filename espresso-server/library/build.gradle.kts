plugins {
    id("com.android.library")
    kotlin("android")
}

val appiumCompileSdk: String by project
val appiumMinSdk: String by project
val appiumTargetSdk: String by project
val appiumBuildTools: String by project
val appiumSourceCompatibility: String by project
val appiumTargetCompatibility: String by project
val appiumJvmTarget: String by project
val appiumKotlin: String by project
val appiumAndroidxTestVersion: String by project
val appiumAnnotationVersion: String by project
val appiumComposeVersion: String by project
val appiumGsonVersion: String by project
val appiumEspressoVersion: String by project
val appiumNanohttpdVersion: String by project
val appiumRobolectricVersion: String by project
val appiumJUnitVersion: String by project
val appiumUiAutomatorVersion: String by project

android {
    compileSdk = appiumCompileSdk.toInt()
    buildToolsVersion = appiumBuildTools
    namespace = "io.appium.espressoserver.lib"

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

    kotlinOptions {
        jvmTarget = appiumJvmTarget
    }

    lint {
        targetSdk = appiumTargetSdk.toInt()
    }

    testOptions {
        targetSdk = appiumTargetSdk.toInt()
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // additionalAppDependencies placeholder (don't change or delete this line)

    api("androidx.annotation:annotation:$appiumAnnotationVersion")
    api("androidx.test.espresso:espresso-contrib:$appiumEspressoVersion") {
        // Exclude transitive dependencies to limit conflicts with AndroidX libraries from AUT.
        // Link to PR with fix and discussion https://github.com/appium/appium-espresso-driver/pull/596
        isTransitive = false
    }
    api("androidx.test.espresso:espresso-web:$appiumEspressoVersion") {
        because("Espresso Web Atoms support (mobile: webAtoms)")
    }
    api("androidx.test.uiautomator:uiautomator:$appiumUiAutomatorVersion") {
        because("UiAutomator support (mobile: uiautomator)")
    }
    api("androidx.test:core:$appiumAndroidxTestVersion")
    api("androidx.test:runner:$appiumAndroidxTestVersion")
    api("androidx.test:rules:$appiumAndroidxTestVersion")
    api("com.google.code.gson:gson:$appiumGsonVersion")
    api("org.nanohttpd:nanohttpd-webserver:$appiumNanohttpdVersion")
    api("org.jetbrains.kotlin:kotlin-stdlib:$appiumKotlin")
    api("org.jetbrains.kotlin:kotlin-reflect:$appiumKotlin")
    api("androidx.compose.ui:ui-test:$appiumComposeVersion") {
        because("Android Compose support")
    }
    api("androidx.compose.ui:ui-test-junit4:$appiumComposeVersion") {
        because("Android Compose support")
    }

    testImplementation("androidx.test.espresso:espresso-contrib:$appiumEspressoVersion")
    testImplementation("junit:junit:$appiumJUnitVersion")
    testImplementation("org.robolectric:robolectric:$appiumRobolectricVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$appiumKotlin")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$appiumKotlin")

    constraints {
        api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$appiumKotlin")
    }
}
