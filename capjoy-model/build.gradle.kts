plugins {
    kotlin("multiplatform") version "2.0.10"
    kotlin("plugin.serialization") version "2.0.10"
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    macosArm64("native") {
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
            }
        }
    }
}
