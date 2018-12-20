package com.trello.identifier.plugin

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.trello.identifier.annotation.PackageId

class RealPackageIdentifierCompiler : PackageIdentifierCompiler {

  override fun createPackageIdentifierFile(packageName: String, output: Appendable, isDebuggable: Boolean) {
    val pId = AnnotationSpec.builder(PackageId::class)
        .apply { addMember("isDebug = %L", isDebuggable) }
        .build()
    val id = TypeSpec.classBuilder(ClassName(packageName, "PackageIdentifier"))
        .addModifiers(KModifier.PUBLIC)
        .addAnnotation(pId)
        .build()

    val file = FileSpec.builder(packageName, "PackageIdentifier")
        .addType(id)
        .addComment("Generated, do not modify!")
        .build()
    file.writeTo(output)
  }
}