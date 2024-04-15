plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotest.multiplatform)
}

kotlin {
    macosX64 {
        binaries {
            executable {
                entryPoint = "io.watters.keystats.main"
            }
        }
    }
    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)

                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.framework.datatest)
                implementation(libs.kotest.assertions.core)
            }
        }
    }
}
