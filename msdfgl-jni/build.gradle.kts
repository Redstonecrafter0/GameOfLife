plugins {
    kotlin("multiplatform") version "1.6.10"
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

group = "net.redstonecraft"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        compilations.getByName("main") {
            cinterops {
                val msdfgl by creating
                val jni by creating {
                    compilerOpts("-I${System.getProperty("java.home")}/include", "-I${System.getProperty("java.home")}/include/win32")
                    linkerOpts("-L${System.getProperty("java.home")}/lib")
                }
            }
        }
        binaries {
            sharedLib {
                baseName = "msdfgl-jni"
            }
        }
    }
    sourceSets {
        val nativeMain by getting
    }
}
