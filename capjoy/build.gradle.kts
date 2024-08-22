plugins {
    kotlin("multiplatform") version "2.0.20"
    kotlin("plugin.power-assert") version "2.0.20"
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    macosArm64("native") {
        binaries {
            executable()
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("com.github.ajalt.clikt:clikt:4.4.0")
                implementation(project(":capjoy-model"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            }
        }
    }
}

// ./gradlew :capjoy:runDebug -PexecArgs="displays"
tasks.register<Exec>("runDebug") {
    dependsOn("linkDebugExecutableNative")
    val arguments = project.findProperty("execArgs")?.toString()?.split(" ") ?: listOf()
    args(arguments)
    environment("CAPJOY_GRADLE_RUN_DEBUG", "true")
    executable = layout.buildDirectory
        .file("bin/native/debugExecutable/capjoy.kexe")
        .get()
        .asFile.absolutePath
    standardInput = System.`in`
}
