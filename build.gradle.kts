import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.9.10"
    application
    id("org.graalvm.buildtools.native") version "0.9.27"

    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
}

group = "de.cramer"
version = "1.0-SNAPSHOT"

val isCi = System.getenv("CI") == "true"

application {
    mainClass = "de.cramer.compiler.MainKt"
}

tasks.named<JavaExec>("run").configure {
    standardInput = System.`in`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.fusesource.jansi:jansi:2.4.0")

    val junitVersion = "5.10.0"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("com.willowtreeapps.assertk:assertk:0.27.0")
}

val configureJavaToolchain = Action<JavaToolchainSpec> {
    languageVersion = JavaLanguageVersion.of(17)
    vendor = JvmVendorSpec.ORACLE
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(configureJavaToolchain)
}

graalvmNative {
    binaries {
        named("main") {
            javaLauncher.set(javaToolchains.launcherFor(configureJavaToolchain))
        }
    }
}

ktlint {
    version.set("1.0.0")
    additionalEditorconfig.set(
        mapOf(
            "ktlint_code_style" to "intellij_idea",
        ),
    )
    reporters {
        reporter(if (isCi) ReporterType.CHECKSTYLE else ReporterType.HTML)
    }
}

detekt {
    basePath = rootProject.projectDir.absolutePath
    buildUponDefaultConfig = true
    config.setFrom(files(".config/detekt.yml"))
}
