package com.trello.mrclean.plugin

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

class RootFunctionGenerator {
  fun createRootFunction(packageName: String, useReflection: Boolean): FileSpec {
    val rootFunction = FunSpec.builder("sanitizedToString")
        .addModifiers(KModifier.INTERNAL)
        .receiver(Any::class)
        .returns(String::class)
        .apply {
          if (useReflection) {
            addStatement("return reflectedToString()")
          }
          else {
            addStatement("return error(%S)", "No function generated! Make sure to annotate with @Sanitize")
          }
        }
        .build()
    return FileSpec.builder(packageName, "RootSanitizeFunction")
        .apply {
          // Switch to using %M with `MemberName` when updated to kotlinpoet 1.3
          if (useReflection) {
            addImport("com.trello.mrclean.reflect", "reflectedToString")
          }
        }
        .addFunction(rootFunction)
        .addComment("This is the root function that generated functions will overload")
        .build()
  }
}