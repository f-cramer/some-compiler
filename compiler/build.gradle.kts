plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":processor-api"))
    ksp(project(":processor"))
}
