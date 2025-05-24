plugins {
    kotlin("jvm") version "2.1.20"
    application
    id("edu.sc.seis.launch4j") version "3.0.5"
    id("org.openjfx.javafxplugin") version "0.1.0"
}

application {
    mainClass.set("my.mjba.CS2Offsets.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "my.mjba.CS2Offsets.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.register<Copy>("copyManifest") {
    from("src/main/resources/admin.manifest")
    into("build/resources/main")
}

tasks.named("processResources") {
    dependsOn("copyManifest")
}

launch4j {
    xmlFileName = "src/main/resources/launch4j-config.xml"
}

group = "my.mjba.CS2Offsets"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.8.0")
    implementation("org.openjfx:javafx-controls:21:win")
    implementation("org.openjfx:javafx-fxml:21:win")
    implementation("org.openjfx:javafx-graphics:21:win")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics")
}

application {
    applicationDefaultJvmArgs = listOf(
        "--add-modules=javafx.controls,javafx.fxml",
        "--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED"
    )
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}