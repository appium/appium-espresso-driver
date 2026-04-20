plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

val javaVersion = maxOf(JavaVersion.current().majorVersion.toInt(), 17)
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}
