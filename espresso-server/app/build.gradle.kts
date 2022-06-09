plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = getIntProperty("appiumCompileSdk", 30)
    buildToolsVersion = getStringProperty("appiumBuildTools", "30.0.3")
    defaultConfig {
        // <instrumentation android:targetPackage=""/>
        applicationId = getStringProperty("appiumTargetPackage", "io.appium.espressoserver")
        // <manifest package=""/>
        testApplicationId = "io.appium.espressoserver.test"
        testHandleProfiling = false
        testFunctionalTest = false
        minSdk = getIntProperty("appiumMinSdk", 21)
        targetSdk = getIntProperty("appiumTargetSdk", 28)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isZipAlignEnabled = getBooleanProperty("appiumZipAlign", true)
        }
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
                storeFile = File(it.toString())
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
        sourceCompatibility = JavaVersion.valueOf(
            getStringProperty(
                "appiumSourceCompatibility",
                "VERSION_1_8"
            ).toUpperCase()
        )
        targetCompatibility = JavaVersion.valueOf(
            getStringProperty(
                "appiumTargetCompatibility",
                "VERSION_1_8"
            ).toUpperCase()
        )
    }

    kotlinOptions {
        jvmTarget = getStringProperty("appiumJvmTarget", JavaVersion.VERSION_1_8.toString())
    }

    packagingOptions {
        resources.excludes.add("META-INF/**")
    }
}

val kotlinVersion = rootProject.extra["appiumKotlin"]
val composeVersion = getStringProperty("appiumComposeVersion", Version.compose)
val annotationVersion = getStringProperty("appiumAnnotationVersion", Version.annotation)

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    // additionalAppDependencies placeholder (don't change or delete this line)

    testImplementation("org.powermock:powermock-api-mockito2:${Version.mocklib}")
    testImplementation("org.powermock:powermock-classloading-xstream:${Version.mocklib}")
    testImplementation("org.powermock:powermock-module-junit4-rule:${Version.mocklib}")
    testImplementation("org.powermock:powermock-module-junit4:${Version.mocklib}")
    testImplementation("androidx.annotation:annotation:${annotationVersion}")
    testImplementation("androidx.test.espresso:espresso-contrib:${Version.espresso}")
    testImplementation("androidx.test.espresso:espresso-core:${Version.espresso}")
    testImplementation("androidx.test.espresso:espresso-web:${Version.espresso}")
    testImplementation("androidx.test.uiautomator:uiautomator:${Version.uia}")
    testImplementation("androidx.test:core:${Version.testlib}")
    testImplementation("androidx.test:runner:${Version.testlib}")
    testImplementation("androidx.test:rules:${Version.testlib}")
    testImplementation("com.google.code.gson:gson:${Version.gson}")
    testImplementation("junit:junit:${Version.junit}")
    testImplementation("org.mockito:mockito-core:${Version.mockito}")
    testImplementation("org.nanohttpd:nanohttpd-webserver:${Version.nanohttpd}")
    testImplementation("org.robolectric:robolectric:${Version.robolectric}")
    testImplementation("org.jetbrains.kotlin:kotlin-test:${kotlinVersion}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    testImplementation("androidx.compose.ui:ui-test:${composeVersion}")
    testImplementation("androidx.compose.ui:ui-test-junit4:${composeVersion}")

    androidTestImplementation("androidx.annotation:annotation:${annotationVersion}")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:${Version.espresso}") {
        // Exclude transitive dependencies to limit conflicts with AndroidX libraries from AUT.
        // Link to PR with fix and discussion https://github.com/appium/appium-espresso-driver/pull/596
        isTransitive = false
    }
    androidTestImplementation("androidx.test.espresso:espresso-web:${Version.espresso}")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:${Version.uia}")
    androidTestImplementation("androidx.test:core:${Version.testlib}")
    androidTestImplementation("androidx.test:runner:${Version.testlib}")
    androidTestImplementation("androidx.test:rules:${Version.testlib}")
    androidTestImplementation("com.google.code.gson:gson:${Version.gson}")
    androidTestImplementation("org.nanohttpd:nanohttpd-webserver:${Version.nanohttpd}")
    androidTestImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    androidTestImplementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    androidTestImplementation("androidx.compose.ui:ui-test:${composeVersion}")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${composeVersion}"){
        isTransitive = false
    }

    // additionalAndroidTestDependencies placeholder (don't change or delete this line)
}

configurations.all {
    resolutionStrategy.eachDependency {
        // To avoid "androidx.annotation:annotation" version conflict.
        if (requested.group == "androidx.annotation" && !requested.name.contains("annotation")) {
            useVersion(annotationVersion)
        }
    }
}

tasks.withType<Test> {
    systemProperty("skipespressoserver", "true")
}

object Version {
    const val espresso = "3.4.0"
    const val testlib = "1.4.0"
    const val mocklib = "2.0.9"
    const val gson = "2.8.9"
    const val uia = "2.2.0"
    const val nanohttpd = "2.3.1"
    const val annotation = "1.3.0"
    const val mockito = "4.0.0"
    const val robolectric = "4.5.1"
    const val junit = "4.13.2"
    const val compose = "1.0.5"
}

fun Project.getStringProperty(name: String, default: String): String =
    properties.getOrDefault(name, default).toString()

fun Project.getIntProperty(name: String, default: Int): Int =
    this.getStringProperty(name, default.toString()).toInt()

fun Project.getBooleanProperty(name: String, default: Boolean): Boolean =
    this.getStringProperty(name, default.toString()).toBoolean()
