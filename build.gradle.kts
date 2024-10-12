plugins {
    `java-library`
}

group = "me.duart"
version = "0.0.2"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val properties = mapOf(
            "version" to project.version,
            "name" to project.name,
            "apiVersion" to "1.21"
        )
        inputs.properties(properties)
        filesMatching("plugin.yml") {
            expand(properties)
        }
    }
}