plugins {
    id("java")
    id("application")
    kotlin("jvm") version "2.0.20"
    id("io.spring.dependency-management") version "1.0.7.RELEASE"
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(platform("io.projectreactor:reactor-bom:2023.0.7"))
    implementation("io.projectreactor:reactor-core")
    implementation("org.example:legacy-lib:1.0-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains:annotations:26.0.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation(kotlin("test"))
}


tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

javafx {
    version = "21.0.4"
    modules = listOf("javafx.controls")
}

application {
    mainClass.set("org.example.GameOfLifeLauncher")
}
