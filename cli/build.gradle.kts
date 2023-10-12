plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":compiler"))
    implementation("org.fusesource.jansi:jansi:2.4.1")
}

application {
    mainClass = "de.cramer.compiler.cli.MainKt"
}

tasks.named<JavaExec>("run").configure {
    standardInput = System.`in`
}
