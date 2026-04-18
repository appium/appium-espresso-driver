import java.io.File

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    // Match [versions] androidGradlePlugin in gradle/libs.versions.toml (same key Dependabot updates).
    // Espresso driver passes -PappiumAndroidGradlePlugin when espressoBuildConfig.toolsVersions.androidGradlePlugin is set.
    val agpDefault =
        Regex("""(?m)^androidGradlePlugin\s*=\s*"([^"]+)"""")
            .find(File(rootDir, "gradle/libs.versions.toml").readText())
            ?.groupValues
            ?.get(1)
            ?: error("androidGradlePlugin not found in gradle/libs.versions.toml")

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application" || requested.id.id == "com.android.library") {
                val fromCapability =
                    providers.gradleProperty("appiumAndroidGradlePlugin").orNull?.trim()?.takeIf { it.isNotEmpty() }
                useVersion(fromCapability ?: agpDefault)
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

include(":app")
include(":library")
