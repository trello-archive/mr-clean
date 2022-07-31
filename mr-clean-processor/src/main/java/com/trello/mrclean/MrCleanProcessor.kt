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

package com.trello.mrclean

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import com.trello.mrclean.annotations.Sanitize
import kotlinx.metadata.impl.extensions.MetadataExtensions
import kotlinx.metadata.jvm.KotlinClassMetadata
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

@AutoService(Processor::class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
class MrCleanProcessor : AbstractProcessor() {

  private lateinit var messager: Messager
  private lateinit var elementUtils: Elements
  private lateinit var typeUtils: Types
  private lateinit var filer: Filer
  private var generatedDir: File? = null
  private var isDebug: Boolean = false
  private var packageName: String? = null

  private val sanitize = Sanitize::class.java

  override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(
      sanitize.canonicalName
  )

  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

  override fun getSupportedOptions() = mutableSetOf(
    OPTION_KAPT_GENERATED,
    OPTION_DEBUG,
    OPTION_PACKAGE_NAME
  )

  override fun init(processingEnv: ProcessingEnvironment) {
    super.init(processingEnv)
    messager = processingEnv.messager
    elementUtils = processingEnv.elementUtils
    typeUtils = processingEnv.typeUtils
    filer = processingEnv.filer
    generatedDir = processingEnv.options[OPTION_KAPT_GENERATED]?.let(::File)
    // load properties applied by MrCleanPlugin
    isDebug = processingEnv.options[OPTION_DEBUG] == "true"
    packageName = processingEnv.options[OPTION_PACKAGE_NAME]
  }

  override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
    val funs = roundEnv.getElementsAnnotatedWith(sanitize)
        .map {
          val classHeader = it.getClassHeader()!!
          val metadata = classHeader.readKotlinClassMetadata()
          it to when (metadata) {
            is KotlinClassMetadata.Class -> metadata.readClassData()
            else -> error("not a class: $metadata")
          }
        }
        .map { (element, classData) ->
          element to SanitizeGenerator.generateSanitizedToString(classData, isDebug)
              .toBuilder()
              .addOriginatingElement(element)
              .build()
        }

    if (funs.isNotEmpty() && packageName == null) {
      messager.printMessage(Diagnostic.Kind.ERROR, "PackageName is not available at processing time. You may be trying to annotate test classes, which is unsupported.")
      return true
    }

    funs.map { (element, funSpec) ->
      val enclosingElementName = element.enclosingElement.simpleName.toString().capitalize()
      FileSpec.builder(packageName!!, "SanitizationFor$enclosingElementName${element.simpleName}")
          .apply {
            if (isDebug) addFileComment("Debug") else addFileComment("Release")
          }
          .addFunction(funSpec)
          .build()
    }
        .forEach { it.writeTo(processingEnv.filer) }

    return true
  }


  companion object {
    /**
     * Name of the processor option containing the path to the Kotlin generated src dir.
     */
    private const val OPTION_KAPT_GENERATED = "kapt.kotlin.generated"

    /**
     * Compiler options that get added by MrCleanPlugin
     * mrclean.debug - whether the variant's build type is debuggable
     * mrclean.packagename - the root package name for the project
     *
     * Changes here must be reflected in MrCleanPlugin.kt
     */
    private const val OPTION_DEBUG = "mrclean.debug"
    private const val OPTION_PACKAGE_NAME = "mrclean.packagename"

    init {
      // https://youtrack.jetbrains.net/issue/KT-24881
      with(Thread.currentThread()) {
        val classLoader = contextClassLoader
        contextClassLoader = MetadataExtensions::class.java.classLoader
        try {
          MetadataExtensions.INSTANCES
        } finally {
          contextClassLoader = classLoader
        }
      }
    }
  }
}

fun Messager.note(message: String) {
  printMessage(Diagnostic.Kind.NOTE, message)
}

