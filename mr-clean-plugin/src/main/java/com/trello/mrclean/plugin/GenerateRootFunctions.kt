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

  @get:Input
  var useReflection: Boolean? = null

  @Suppress("unused") // Invoked by Gradle.
  @TaskAction
  fun brewJava() {
    brewJava(outputDir!!, packageName!!, useReflection!!)
  }
}

fun brewJava(outputDir: File, packageName: String, useReflection: Boolean) {
  val compiler = RootFunctionGenerator()
  compiler.createRootFunction(packageName, useReflection).writeTo(outputDir)
}