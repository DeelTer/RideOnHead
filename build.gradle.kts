plugins {
    java
    id("com.gradleup.shadow") version "9.4.1"
}

group = "ru.deelter"
version = "1.1.2"

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
    relocate("org.bstats", "ru.deelter.rideonhead.libs.bstats")
    relocate("com.github.benmanes.caffeine", "ru.deelter.rideonhead.libs.caffeine")

    archiveClassifier = ""
}

tasks.build {
    dependsOn(tasks.shadowJar)
}