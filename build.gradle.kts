plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    jacoco
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

dependencies {
    val springBootVersion = libs.versions.springBoot.get()
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    implementation(platform(libs.modulith.bom))
    implementation(platform(libs.jmolecules.bom))

    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.jdbc)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.modulith.starter.core)
    implementation(libs.modulith.starter.jdbc)
    implementation(libs.jmolecules.hexagonal)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.commons.validator)
    implementation(libs.kotlin.logging)
    runtimeOnly(libs.kotlin.reflect)
    runtimeOnly(libs.hsqldb)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.modulith.starter.test)
    testImplementation(libs.archunit.junit5)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.jmolecules.archunit)
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    // JDK 21+ warns on dynamic agent attach; a future JDK will disallow it.
    jvmArgumentProviders.add(
        CommandLineArgumentProvider {
            val mockitoAgent =
                classpath.files.single { it.name.startsWith("mockito-core-") && it.extension == "jar" }
            listOf(
                "-javaagent:${mockitoAgent.absolutePath}",
                // The agent appends the bootstrap classpath, which disables CDS for app classes.
                "-Xshare:off",
            )
        },
    )
}

val modulithModules = listOf("links", "clicks", "analytics", "streaming")

val jacocoModuleReports =
    modulithModules.map { module ->
        tasks.register<JacocoReport>("jacoco${module.replaceFirstChar { it.uppercase() }}Report") {
            group = "verification"
            description = "JaCoCo HTML/XML report for the $module module."
            dependsOn(tasks.test)
            executionData.setFrom(layout.buildDirectory.file("jacoco/test.exec"))
            sourceDirectories.setFrom(files("src/main/kotlin"))
            classDirectories.setFrom(
                sourceSets.main.get().output.classesDirs.map { classesDir ->
                    fileTree(classesDir) {
                        include("es/unizar/urlshortener/$module/**")
                    }
                },
            )
            reports {
                html.required.set(true)
                html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/$module/html"))
                xml.required.set(true)
                xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/$module/jacoco.xml"))
            }
        }
    }

tasks.jacocoTestReport {
    dependsOn(tasks.test, jacocoModuleReports)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.60".toBigDecimal() // 60% line coverage
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$projectDir/config/detekt.yml"))
}

ktlint {
    version.set("1.5.0")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("urlshortener.jar")
}
