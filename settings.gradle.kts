pluginManagement {
    plugins {
        val kotlinVersion = extra["KOTLIN_VERSION"] as String
        id("org.jetbrains.kotlin.jvm") version kotlinVersion

        val kspVersion = extra["KSP_VERSION"] as String
        id("com.google.devtools.ksp") version "$kotlinVersion-$kspVersion"
    }
}

rootProject.name = "compiler"

include("cli")
include("compiler")
include("processor")
include("processor-api")
