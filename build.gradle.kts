plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

group = "net.okocraft"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly("dev.folia:folia-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.3")

    compileOnly("net.luckperms:api:5.4")

    implementation("com.github.siroshun09.configapi:configapi-yaml:4.6.3")
    implementation("com.github.siroshun09.translationloader:translationloader:2.0.2")
}

tasks {
    compileJava {
        options.release.set(17)
    }

    processResources {
        filesMatching(listOf("plugin.yml", "en.yml", "ja_JP.yml")) {
            expand("projectVersion" to version)
        }
    }

    shadowJar {
        relocate("com.github.siroshun09", "${group}.${name.toLowerCase()}.lib")
    }

    build {
        dependsOn(shadowJar)
    }
}

