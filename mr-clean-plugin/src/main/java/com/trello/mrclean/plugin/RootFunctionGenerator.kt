package com.trello.mrclean.plugin

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

class RootFunctionGenerator {
  fun createRootFunction(packageName: String): FileSpec {
    val rootFunction = FunSpec.builder("sanitizedToString")
        .addModifiers(KModifier.INTERNAL)
        .receiver(Any::class)
        .returns(String::class)
        .addStatement("return error(%S)", "No function generated! Make sure to annotate with @Sanitize")
        .build()
    return FileSpec.builder(packageName, "RootSanitizeFunction")
        .addFunction(rootFunction)
        .addFileComment("This is the root function that generated functions will overload")
        .build()
  }
}