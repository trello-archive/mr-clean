package com.trello.mrclean.plugin

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec

class RootFunctionGenerator {
  fun createRootFunction(packageName: String): FileSpec {
    val rootFunction = FunSpec.builder("sanitizedToString")
        .receiver(Any::class)
        .returns(String::class)
        .apply {
          addStatement("return error(%S)", "No function generated! Make sure to annotate with @Sanitize")
        }
        .build()
    return FileSpec.builder(packageName, "RootSanitizeFunction")
        .addFunction(rootFunction)
        .addComment("This is the root function that generated functions will overload")
        .build()
  }
}