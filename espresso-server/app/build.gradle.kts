plugins {
    alias(libs.plugins.android.application)
}

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.ScopedArtifacts
import io.appium.espressoserver.gradle.RelocateClassesTask
import io.appium.espressoserver.gradle.removeJarEntries
import io.appium.espressoserver.gradle.withCapabilityVersion
import io.appium.espressoserver.jvmtarget.AppiumJvmTarget
import org.gradle.api.GradleException
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.attributes.Attribute
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
val shadeServer: Boolean =
    (findProperty("appiumShadeServer") as String?)?.equals("false", ignoreCase = true) != true
// Comma-separated group:module coordinates of the user's additional dependencies, kept unrelocated
val sharedCoordinates: Set<String> =
    (findProperty("appiumSharedDependencies") as String?)?.split(",")?.map { it.trim() }
        ?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()

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
        if (shadeServer) {
            // Only the service files stay: the relocation task adds the ones of the relocated interfaces
            // (a negated class excludes every META-INF folder that does not start with "s")
            resources.excludes.addAll(listOf("META-INF/*", "META-INF/[!s]*/**", "META-INF/s[!e]*/**"))
            resources.pickFirsts.add("META-INF/services/**")
        } else {
            resources.excludes.add("META-INF/**")
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

if (shadeServer) {
    val shadedPrefix = "io/appium/espressoserver/shaded/"
    androidComponents {
        onVariants { variant ->
            val androidTest = variant.androidTest ?: return@onVariants
            val suffix = variant.name.replaceFirstChar { it.uppercase() }
            val relocateClasses = tasks.register<RelocateClassesTask>("relocate${suffix}AndroidTestClasses") {
                prefix.set(shadedPrefix)
                excludedResources.set(layout.buildDirectory.file("shade/$suffix/excluded-resources.txt"))
                keep.set(
                    listOf(
                        // Server and the already shaded XPath 2 processor
                        "io/appium/",
                        // Package-private access between the runner and its superclass
                        "androidx/test/runner/",
                        "androidx/test/core/app/InstrumentationActivityInvoker",
                    ),
                )
                shared.set(
                    // Compose test code needs the AUT's own Compose classes (static registries)
                    (if (useComposeLibrary) listOf("androidx/compose/", "kotlin/", "kotlinx/") else emptyList()) +
                        // The AUT implements IdlingResource and hands it to the server
                        listOf("androidx/test/espresso/IdlingResource", "androidx/test/espresso/idling/"),
                )
                sharedExclude.set(listOf("androidx/compose/ui/test/"))
                if (sharedCoordinates.isNotEmpty()) {
                    userJars.from(
                        androidTest.runtimeConfiguration.incoming.artifactView {
                            attributes {
                                attribute(Attribute.of("artifactType", String::class.java), "android-classes-jar")
                            }
                            lenient(true)
                            componentFilter { id ->
                                id is ModuleComponentIdentifier && "${id.group}:${id.module}" in sharedCoordinates
                            }
                        }.files,
                    )
                }
            }
            androidTest.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                .use(relocateClasses)
                .toTransform(
                    ScopedArtifact.CLASSES,
                    RelocateClassesTask::allJars,
                    RelocateClassesTask::allDirectories,
                    RelocateClassesTask::output,
                )
            // Rewritten service files of the relocated interfaces replace the originals, which list
            // providers that no longer exist under these names
            androidTest.sources.resources?.addGeneratedSourceDirectory(relocateClasses, RelocateClassesTask::servicesDir)
            tasks.matching { it.name == "merge${suffix}AndroidTestJavaResource" }.configureEach {
                val excluded = relocateClasses.flatMap { it.excludedResources }
                inputs.file(excluded)
                doLast {
                    val names = excluded.get().asFile.readLines().filter(String::isNotBlank).toSet()
                    outputs.files.filter { it.name.endsWith(".jar") }.forEach { removeJarEntries(it, names) }
                }
            }
        }
    }
}
