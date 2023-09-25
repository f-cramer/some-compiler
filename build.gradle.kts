import io.gitlab.arturbosch.detekt.Detekt
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.9.10"

    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
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

allprojects {
    repositories {
        mavenCentral()
    }

    plugins.withId(kotlinPluginId) {
        apply(plugin = ktlintPluginId)
        apply(plugin = detektPluginId)

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
            val junitVersion = "5.10.0"
            testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
            testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
            testImplementation("com.willowtreeapps.assertk:assertk:0.27.0")
        }
    }
}
