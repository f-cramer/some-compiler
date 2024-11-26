import io.gitlab.arturbosch.detekt.Detekt
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm")

    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
    id("io.gitlab.arturbosch.detekt") version "1.23.7"

    id("com.google.devtools.ksp")
}

group = "de.cramer"
version = "1.0-SNAPSHOT"

val isCi = System.getenv("CI") == "true"

val configureJavaToolchain = Action<JavaToolchainSpec> {
    languageVersion = JavaLanguageVersion.of(17)
}

tasks.withType<JavaExec>().configureEach {
    outputs.upToDateWhen { false }
    javaLauncher.set(javaToolchains.launcherFor(configureJavaToolchain))
}

val kotlinPluginId = "org.jetbrains.kotlin.jvm"
val ktlintPluginId = "org.jlleitschuh.gradle.ktlint"
val detektPluginId = "io.gitlab.arturbosch.detekt"
val kspPluginid = "com.google.devtools.ksp"

allprojects {
    repositories {
        mavenCentral()
    }

    plugins.withId(kotlinPluginId) {
        apply(plugin = ktlintPluginId)
        apply(plugin = detektPluginId)
        apply(plugin = kspPluginid)

        kotlin {
            jvmToolchain(configureJavaToolchain)
        }

        tasks.test {
            useJUnitPlatform()
        }
    }

    plugins.withId(ktlintPluginId) {
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
    }

    plugins.withId(detektPluginId) {
        detekt {
            basePath = rootProject.projectDir.absolutePath
            buildUponDefaultConfig = true
            config.setFrom(rootProject.files(".config/detekt.yml"))
        }

        tasks.named<Detekt>("detekt").configure {
            reports {
                val file = rootProject.file("build/reports/detekt/${project.path.replace(":", ".")}.sarif")
                file.parentFile.mkdirs()
                sarif.outputLocation.set(file)
            }
        }
    }
}

subprojects {
    plugins.withId(kotlinPluginId) {
        dependencies {
            testImplementation(platform("org.junit:junit-bom:5.11.4"))
            testImplementation("org.junit.jupiter:junit-jupiter-api")
            testImplementation("org.junit.jupiter:junit-jupiter-params")
            testRuntimeOnly("org.junit.platform:junit-platform-launcher")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
            testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
        }
    }
}
