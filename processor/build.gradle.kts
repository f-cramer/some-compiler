plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":processor-api"))

    val kotlinVersion = properties["KOTLIN_VERSION"] as String
    val kspVersion = properties["KSP_VERSION"] as String
    implementation("com.google.devtools.ksp:symbol-processing-api:$kotlinVersion-$kspVersion")

    implementation("com.squareup:kotlinpoet-ksp:1.18.0")
}
