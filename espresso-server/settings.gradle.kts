pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    // When Espresso driver passes -PappiumAndroidGradlePlugin, override the AGP version from the
    // version catalog. Otherwise keep Gradle's catalog-resolved requested version (no regex/TOML parse).
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application" || requested.id.id == "com.android.library") {
                val fromCapability =
                    providers.gradleProperty("appiumAndroidGradlePlugin").orNull?.trim()?.takeIf { it.isNotEmpty() }
                if (fromCapability != null) {
                    useVersion(fromCapability)
                }
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
