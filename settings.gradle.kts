pluginManagement {
    plugins {
        val kotlinVersion = extra["KOTLIN_VERSION"] as String
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
    }
}

rootProject.name = "compiler"

include("cli")
include("compiler")
