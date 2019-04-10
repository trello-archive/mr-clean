package com.trello.mrclean.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GenerateRootFunctions : DefaultTask() {
  @get:OutputDirectory
  var outputDir: File? = null

  @get:Input
  var packageName: String? = null

  @Suppress("unused") // Invoked by Gradle.
  @TaskAction
  fun brewJava() {
    brewJava(outputDir!!, packageName!!)
  }
}

fun brewJava(outputDir: File, packageName: String) {
  val compiler = RootFunctionGenerator()
  compiler.createRootFunction(packageName).writeTo(outputDir)
}