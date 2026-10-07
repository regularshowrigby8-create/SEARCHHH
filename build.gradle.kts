// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}

// No debt baseline: existing violations must fail until reviewed and resolved.
detekt {
    toolVersion = libs.versions.detekt.get()
    buildUponDefaultConfig = true
    config.setFrom(files("quality/detekt.yml"))
    source.setFrom(files("app/src", "ad-filter/src", "adblock-client/src"))
    ignoreFailures = false
}

ktlint {
    version.set(libs.versions.ktlint.get())
    ignoreFailures.set(false)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set(
            rootProject.libs.versions.ktlint
                .get(),
        )
        ignoreFailures.set(false)
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions.allWarningsAsErrors.set(true)
    }
    plugins.withId("com.android.library") {
        extensions.configure<com.android.build.gradle.LibraryExtension> {
            lint {
                abortOnError = true
                warningsAsErrors = true
                checkReleaseBuilds = true
                checkDependencies = true
            }
        }
    }
}

tasks.named("ktlintCheck") {
    dependsOn(subprojects.map { "${it.path}:ktlintCheck" })
}

val beginVerificationRun by tasks.registering(Exec::class) {
    group = "verification"
    description = "Record provenance before fresh Android test execution."
    commandLine("python3", "tools/quality/start_run.py")
}

subprojects {
    tasks.matching { it.name in setOf("testDebugUnitTest", "testReleaseUnitTest", "connectedDebugAndroidTest") }.configureEach {
        dependsOn(beginVerificationRun)
        if (project.name == "app" && name == "connectedDebugAndroidTest") {
            mustRunAfter(":ad-filter:connectedDebugAndroidTest", ":adblock-client:connectedDebugAndroidTest")
        }
        outputs.upToDateWhen { false }
        outputs.cacheIf { false }
    }
}

val qualityToolTests by tasks.registering(Exec::class) {
    group = "verification"
    description = "Exercise the checker itself, including negative fixtures."
    commandLine("python3", "-m", "unittest", "discover", "-s", "tools/quality/tests", "-v")
}

val failOnUnfinishedCode by tasks.registering(Exec::class) {
    group = "verification"
    description = "Reject unfinished production/test source and unreviewed exceptions."
    dependsOn(qualityToolTests)
    commandLine("python3", "tools/quality/unfinished.py")
}

val verifyInteractionInventory by tasks.registering(Exec::class) {
    group = "verification"
    description = "Reject missing or stale control inventory entries."
    commandLine("python3", "tools/quality/interactions.py", "--check")
}

val verifyInteractionEvidence by tasks.registering(Exec::class) {
    group = "verification"
    description = "Require action/state tests and executed evidence for every inventoried control."
    dependsOn(":app:connectedDebugAndroidTest", ":ad-filter:connectedDebugAndroidTest", ":adblock-client:connectedDebugAndroidTest")
    commandLine("python3", "tools/quality/evidence.py")
}

tasks.register("searchhhJvmVerification") {
    group = "verification"
    description = "Host checks only; this is not the full UI verification gate."
    dependsOn(
        failOnUnfinishedCode,
        verifyInteractionInventory,
        "detekt",
        "ktlintCheck",
        ":app:compileDebugKotlin",
        ":app:testDebugUnitTest",
        ":ad-filter:testDebugUnitTest",
        ":adblock-client:testDebugUnitTest",
        ":app:lintDebug",
        ":ad-filter:lintDebug",
        ":adblock-client:lintDebug",
        ":app:assembleDebug",
    )
}

tasks.register("searchhhDeviceEvidence") {
    group = "verification"
    description = """
        Device-lane half of the master gate: instrumented runs plus the app JVM results the
        evidence checker reads in the same workspace. Static host checks stay in
        searchhhJvmVerification, which the parallel CI lanes already execute.
        """.trimIndent()
    dependsOn(
        ":app:testDebugUnitTest",
        verifyInteractionEvidence,
    )
}

tasks.register("searchhhVerification") {
    group = "verification"
    description = "Full mandatory host, device, interaction and visual evidence gate; requires an emulator."
    dependsOn("searchhhJvmVerification", verifyInteractionEvidence)
}
