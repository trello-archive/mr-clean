plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.vanniktech.publish)
    `kotlin-dsl`
}
dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())
    implementation(project(":mr-clean-runtime"))

    implementation(libs.android.gradlePlugin)
    implementation(libs.plugin.ksp)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinPoet.core)

    testImplementation(libs.junit)
}

sourceSets.main {
    kotlin {
        srcDir(layout.buildDirectory.dir("generated/source/mrclean"))
    }
}

gradlePlugin {
    plugins {
        create("mrclean") {
            id = "com.trello.mrclean"
            implementationClass = "com.trello.mrclean.plugin.MrCleanPlugin"
            displayName = "Mr. Clean Plugin"
            description = "Sanitizes classes to avoid leaking sensitive data when using toString()"
        }
    }
}

tasks.register("pluginVersion") {
    val outputDir = layout.buildDirectory.dir("generated/source/mrclean").get().asFile

    inputs.property("version", version)
    outputs.dir(outputDir)

    doLast {
        val versionFile = file("$outputDir/com/trello/mrclean/Version.kt")
        versionFile.parentFile.mkdirs()
        versionFile.writeText(
            """
            // Generated file. Do not edit!
            package com.trello.mrclean
            val VERSION = "${project.version}"
            """.trimIndent()
        )
    }
}

afterEvaluate {
    tasks.named("compileKotlin") { dependsOn("pluginVersion") }
    tasks.named("sourcesJar") { dependsOn("pluginVersion") }
}
