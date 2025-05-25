plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.vanniktech.publish)
    id("java-gradle-plugin")
}
dependencies {
    compileOnly(gradleApi())
    implementation(project(":mr-clean-runtime"))

    implementation(libs.android.gradlePlugin)
    implementation(libs.android.gradlePlugin)
    implementation(libs.plugin.ksp)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinPoet.core)

    testImplementation(libs.junit)
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets.main {
    java {
        srcDir(layout.buildDirectory.dir("generated/source/mrclean"))
    }
}

gradlePlugin {
    plugins {
        create("mrclean") {
            id = "com.trello.mrclean"
            implementationClass = "com.trello.mrclean.plugin.MrCleanPlugin"
        }
    }
}

tasks.register("pluginVersion") {
    val outputDir = file("$buildDir/generated/source/mrclean/")

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
