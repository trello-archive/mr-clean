/*
 * Copyright @ 2018 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trello.sanitize.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.FeatureExtension
import com.android.build.gradle.FeaturePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.trello.sanitize.VERSION
import com.trello.sanitize.internal.PackageIdentifier
import groovy.util.XmlSlurper
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

/**
 * Based on butterknife's multi-module gradle plugin
 */
class MrCleanPlugin : Plugin<Project> {
  private val log by lazy {
    LoggerFactory.getLogger("Mr. Clean")
  }

  override fun apply(project: Project) {
    project.plugins.all {
      when (it) {
        is FeaturePlugin -> {
          project.extensions[FeatureExtension::class].run {
            configureSanitizationGeneration(project, featureVariants)
            configureSanitizationGeneration(project, libraryVariants)
          }
        }
        is LibraryPlugin -> {
          project.extensions[LibraryExtension::class].run {
            configureSanitizationGeneration(project, libraryVariants)
          }
        }
        is AppPlugin -> {
          project.extensions[AppExtension::class].run {
            configureSanitizationGeneration(project, applicationVariants)
          }
        }
      }
    }
  }

  // Parse the variant's main manifest file in order to get the package id which is used to create
  // Sanitizations.kt in the right place.
  private fun getPackageName(variant: BaseVariant): String {
    val slurper = XmlSlurper(false, false)
    val list = variant.sourceSets.map { it.manifestFile }

    // According to the documentation, the earlier files in the list are meant to be overridden by the later ones.
    // So the first file in the sourceSets list should be main.
    val result = slurper.parse(list[0])
    return result.getProperty("@package").toString()
  }

  private fun configureSanitizationGeneration(project: Project, variants: DomainObjectSet<out BaseVariant>) {
    val implDeps = project.configurations.getByName("implementation").dependencies
    implDeps.add(project.dependencies.create("com.trello:mr-clean-annotations:$VERSION"))
    val kaptDeps = project.configurations.getByName("kapt").dependencies
    kaptDeps.add(project.dependencies.create("com.trello:mr-clean-processor:$VERSION"))
    variants.all { variant ->
      val once = AtomicBoolean()

      if (once.compareAndSet(false, true)) {
        val packageName = getPackageName(variant)
        val taskName = "generate${variant.name.capitalize()}PackageInfo"
        val task = project.tasks.create(taskName)
        val outputDir = project.buildDir.resolve("generated/source/mr_clean/${variant.name}")
        task.outputs.dir(outputDir)
        variant.registerJavaGeneratingTask(task, outputDir)
        variant.addJavaSourceFoldersToModel(outputDir)
        task.apply {
          doLast {
            val pId = AnnotationSpec.builder(PackageIdentifier::class)
                .apply {
                  addMember("isDebug = %L", variant.buildType.isDebuggable)
                }
                .build()
            val id = TypeSpec.classBuilder(ClassName(packageName, "MrCleanPackageIdentifier"))
                .addModifiers(KModifier.PUBLIC)
                .addAnnotation(pId)
                .build()

            val file = FileSpec.builder(packageName, "MrCleanPackageIdentifier")
                .addType(id)
                .addComment("Generated for mr. clean, do not modify!")
                .build()
            file.writeTo(outputDir)
          }
        }
      }
    }
  }
}

private operator fun <T : Any> ExtensionContainer.get(type: KClass<T>): T {
  return getByType(type.java)!!
}
