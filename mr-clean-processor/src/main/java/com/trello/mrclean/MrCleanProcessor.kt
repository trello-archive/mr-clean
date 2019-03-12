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
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asTypeName
import com.trello.identifier.annotation.PackageId
import com.trello.mrclean.annotations.Sanitize
import kotlinx.metadata.impl.extensions.MetadataExtensions
import kotlinx.metadata.jvm.KotlinClassMetadata
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
class MrCleanProcessor : AbstractProcessor() {

  private lateinit var messager: Messager
  private lateinit var elementUtils: Elements
  private lateinit var typeUtils: Types
  private lateinit var filer: Filer
  private var generatedDir: File? = null

  private val sanitize = Sanitize::class.java
  private val packageIdentifier = PackageId::class.java

  override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(
      sanitize.canonicalName,
      packageIdentifier.canonicalName
  )

  override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

  override fun init(processingEnv: ProcessingEnvironment) {
    super.init(processingEnv)
    messager = processingEnv.messager
    elementUtils = processingEnv.elementUtils
    typeUtils = processingEnv.typeUtils
    filer = processingEnv.filer
    generatedDir = processingEnv.options[OPTION_KAPT_GENERATED]?.let(::File)

  }

  override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
    val packageIdentifierClass = roundEnv.getElementsAnnotatedWith(packageIdentifier).firstOrNull() ?: return true
    val isDebug = packageIdentifierClass.getAnnotation(packageIdentifier).isDebug
    val targetPackage = elementUtils.getPackageOf(packageIdentifierClass).qualifiedName.toString()
    val funs = roundEnv.getElementsAnnotatedWith(sanitize)
        .map {
          val classHeader = it.getClassHeader()!!
          val metadata = classHeader.readKotlinClassMetadata()
          when (metadata) {
            is KotlinClassMetadata.Class -> metadata.readClassData()
            else -> error("not a class")
          }
        }
        .map {
          SanitizeGenerator.generateSanitizedToString(it, isDebug)
        }

    if (funs.isNotEmpty()) {
      val fileSpecBuilder = FileSpec.builder(targetPackage, "Sanitizations")
      if (isDebug) fileSpecBuilder.addComment("Debug") else fileSpecBuilder.addComment("Release")
      funs.forEach { fileSpecBuilder.addFunction(it) }
      fileSpecBuilder.build().writeTo(generatedDir!!)
    }

    return true
  }


  companion object {
    /**
     * Name of the processor option containing the path to the Kotlin generated src dir.
     */
    private const val OPTION_KAPT_GENERATED = "kapt.kotlin.generated"

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

