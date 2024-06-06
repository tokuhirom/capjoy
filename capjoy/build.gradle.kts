plugins {
    kotlin("multiplatform") version "2.0.0"
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
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.4.0")
            }
        }
    }
}

// ./gradlew :capjoy:runDebug -PexecArgs="displays"
tasks.register<Exec>("runDebug") {
    dependsOn("linkDebugExecutableNative")
    val arguments = project.findProperty("execArgs")?.toString()?.split(" ") ?: listOf()
    args(arguments)
    executable = file("$buildDir/bin/native/debugExecutable/capjoy.kexe").absolutePath
}
