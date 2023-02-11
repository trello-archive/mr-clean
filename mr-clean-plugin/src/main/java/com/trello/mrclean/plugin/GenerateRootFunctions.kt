package com.trello.mrclean.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateRootFunctions : DefaultTask() {
  @get:OutputDirectory
  abstract val outputDir: DirectoryProperty

  @get:Input
  abstract val packageName: Property<String>

  @Suppress("unused") // Invoked by Gradle.
  @TaskAction
  fun brewJava() {
    brewJava(outputDir.asFile.get(), packageName.get())
  }
}

fun brewJava(outputDir: File, packageName: String) {
  val compiler = RootFunctionGenerator()
  compiler.createRootFunction(packageName).writeTo(outputDir)
}