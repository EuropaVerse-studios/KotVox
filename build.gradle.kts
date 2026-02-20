plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "com.gabry.kotvox"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val lwjglVersion = "3.3.3"
val jomlVersion = "1.10.5"

val os = org.gradle.internal.os.OperatingSystem.current()
val lwjglNatives = when {
    os.isWindows -> "natives-windows"
    os.isLinux -> "natives-linux"
    os.isMacOsX -> "natives-macos"
    else -> throw GradleException("Unsupported OS")
}

dependencies {
    // Kotlin Standard Library
    implementation(kotlin("stdlib"))

    // JOML (Java OpenGL Math Library) - Essential for 3D math
    implementation("org.joml:joml:$jomlVersion")

    // LWJGL 3 - Core
    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-stb:$lwjglVersion") // Image loading

    // LWJGL 3 - Natives
    runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-stb:$lwjglVersion:$lwjglNatives")
    
    // ImGui (UI System)
    val imguiVersion = "1.86.11"
    implementation("io.github.spair:imgui-java-binding:$imguiVersion")
    implementation("io.github.spair:imgui-java-lwjgl3:$imguiVersion")
    implementation("io.github.spair:imgui-java-natives-windows:$imguiVersion")
    
    // Test
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("engine.core.KotVoxKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
