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
val appiumAndroidGradlePlugin: String by project
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

    sourceSets {
        getByName("test") {
            java.srcDirs("src/androidTest/java")
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
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

    namespace = "io.appium.espressoserver"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    // additionalAppDependencies placeholder (don't change or delete this line)

    testImplementation("androidx.annotation:annotation:$appiumAnnotationVersion")
    testImplementation("androidx.test.espresso:espresso-contrib:$appiumEspressoVersion")
    testImplementation("androidx.test.espresso:espresso-core:$appiumEspressoVersion")
    testImplementation("androidx.test.espresso:espresso-web:$appiumEspressoVersion")
    testImplementation("androidx.test.uiautomator:uiautomator:$appiumUiAutomatorVersion")
    testImplementation("androidx.test:core:$appiumAndroidxTestVersion")
    testImplementation("androidx.test:runner:$appiumAndroidxTestVersion")
    testImplementation("androidx.test:rules:$appiumAndroidxTestVersion")
    testImplementation("com.google.code.gson:gson:$appiumGsonVersion")
    testImplementation("junit:junit:$appiumJUnitVersion")
    testImplementation("org.mockito:mockito-core:$appiumMockitoVersion")
    testImplementation("org.nanohttpd:nanohttpd-webserver:$appiumNanohttpdVersion")
    testImplementation("org.robolectric:robolectric:$appiumRobolectricVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$appiumKotlin")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$appiumKotlin")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:$appiumKotlin")
    testImplementation("androidx.compose.ui:ui-test:$appiumComposeVersion")
    testImplementation("androidx.compose.ui:ui-test-junit4:$appiumComposeVersion")

    androidTestImplementation("androidx.annotation:annotation:$appiumAnnotationVersion")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:$appiumEspressoVersion") {
        // Exclude transitive dependencies to limit conflicts with AndroidX libraries from AUT.
        // Link to PR with fix and discussion https://github.com/appium/appium-espresso-driver/pull/596
        isTransitive = false
    }
    androidTestImplementation("androidx.test.espresso:espresso-web:$appiumEspressoVersion") {
        because("Espresso Web Atoms support (mobile: webAtoms)")
    }
    androidTestImplementation("androidx.test.uiautomator:uiautomator:$appiumUiAutomatorVersion") {
        because("UiAutomator support (mobile: uiautomator)")
    }
    androidTestImplementation("androidx.test:core:$appiumAndroidxTestVersion")
    androidTestImplementation("androidx.test:runner:$appiumAndroidxTestVersion")
    androidTestImplementation("androidx.test:rules:$appiumAndroidxTestVersion")
    androidTestImplementation("com.google.code.gson:gson:$appiumGsonVersion")
    androidTestImplementation("org.nanohttpd:nanohttpd-webserver:$appiumNanohttpdVersion")
    androidTestImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$appiumKotlin")
    androidTestImplementation("org.jetbrains.kotlin:kotlin-reflect:$appiumKotlin")
    androidTestImplementation("androidx.compose.ui:ui-test:$appiumComposeVersion") {
        because("Android Compose support")
    }
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$appiumComposeVersion") {
        because("Android Compose support")
    }

    // additionalAndroidTestDependencies placeholder (don't change or delete this line)
}

configurations.all {
    resolutionStrategy.eachDependency {
        // To avoid "androidx.annotation:annotation" version conflict.
        if (requested.group == "androidx.annotation" && !requested.name.contains("annotation")) {
            useVersion(appiumAnnotationVersion)
        }
    }
}

tasks.withType<Test> {
    systemProperty("skipespressoserver", "true")
}
