import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

plugins {
    java
    id("com.gradleup.shadow") version "9.4.1"
}

group = "ru.deelter"

val versionFile = file("version.properties")
val versionProps = Properties()
if (versionFile.exists()) {
    versionProps.load(FileInputStream(versionFile))
} else {
    versionProps["major"] = "1"
    versionProps["minor"] = "2"
    versionProps["patch"] = "0"
}

val major = versionProps.getProperty("major").toInt()
val minor = versionProps.getProperty("minor").toInt()
val patch = versionProps.getProperty("patch").toInt()

version = "$major.$minor.$patch"

tasks.register("bumpPatch") {
    doLast {
        versionProps["patch"] = (patch + 1).toString()
        versionProps.store(FileOutputStream(versionFile), null)
        println("Version bumped to $major.$minor.${patch + 1}")
    }
}

tasks.register("bumpMinor") {
    doLast {
        versionProps["minor"] = (minor + 1).toString()
        versionProps["patch"] = "0"
        versionProps.store(FileOutputStream(versionFile), null)
        println("Version bumped to $major.${minor + 1}.0")
    }
}

tasks.register("bumpMajor") {
    doLast {
        versionProps["major"] = (major + 1).toString()
        versionProps["minor"] = "0"
        versionProps["patch"] = "0"
        versionProps.store(FileOutputStream(versionFile), null)
        println("Version bumped to ${major + 1}.0.0")
    }
}


java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    implementation("com.h2database:h2:2.3.232")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("org.bstats:bstats-bukkit:3.2.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 21
}


tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from("src/main/resources") {
        include("lang/**")
    }
}
tasks.shadowJar {
    configurations = project.configurations.runtimeClasspath.map { setOf(it) }
    dependencies {
        exclude { it.moduleGroup != "org.bstats" }
    }
    relocate("org.bstats", project.group.toString())
    relocate("com.github.benmanes.caffeine", "ru.deelter.rideonhead.libs.caffeine")
    relocate("org.h2", "ru.deelter.rideonhead.libs.h2")

    minimize {
        exclude(dependency("com.h2database:h2:.*"))
        exclude(dependency("com.github.ben-manes.caffeine:caffeine:.*"))
    }

    archiveClassifier = ""
}

tasks.build {
    dependsOn(tasks.shadowJar)
}