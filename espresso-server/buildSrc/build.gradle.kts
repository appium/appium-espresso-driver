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

dependencies {
    implementation("org.ow2.asm:asm-commons:9.8")
    testImplementation("junit:junit:4.13.2")
}
