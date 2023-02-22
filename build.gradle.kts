plugins {
    `java-library`
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

group = "net.okocraft"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.1")

    api("net.luckperms:api:5.4")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.compileJava {
    options.release.set(17)
}

tasks.processResources {
    filesMatching(listOf("plugin.yml")) {
        expand("projectVersion" to version)
    }
}

tasks.test {
    useJUnitPlatform()
}
