plugins {
    kotlin("jvm") version "2.2.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/kds/kotlin-ds-maven")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    implementation("org.jetbrains.kotlinx:kandy-lets-plot:0.8.0")
    implementation("org.jetbrains.kotlinx:kotlin-statistics-jvm:0.4.0")
}

tasks {
    test {
        useJUnit()

        include("**/matchers/**")

        afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
            if (desc.parent == null) {
                println("\nTest Results: ${result.resultType}")
                println(
                    "Tests: ${result.testCount}, " +
                            "Successes: ${result.successfulTestCount}, " +
                            "Failures: ${result.failedTestCount}, " +
                            "Skipped: ${result.skippedTestCount}"
                )
            }
        }))
    }
}
kotlin {
    jvmToolchain(21)
}