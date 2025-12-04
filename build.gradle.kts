plugins {
    kotlin("jvm") version "2.2.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
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