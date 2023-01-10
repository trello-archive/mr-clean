package com.trello.mrclean

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asTypeName

internal object SanitizeGenerator {
  fun generateSanitizedToString(classData: ClassData, isDebug: Boolean): FunSpec {
    val debugString = classData.properties.joinToString {
        if (it.isPublic()) "${it.name} = ${"$"}${it.name}" else "${it.name} = <private>"
    }
    val sanitizedOutput = mapOf(
        "className" to classData.className,
        "hexString" to Integer::class.java.asTypeName()
    )
    val suppressAnnotation = AnnotationSpec.builder(Suppress::class)
        .addMember("%S", "NOTHING_TO_INLINE")
        .build()
    val block = CodeBlock.builder()
        .addNamed("return \"%className:L@\${%hexString:T.toHexString(hashCode())}\"\n", sanitizedOutput)
        .build()
    return FunSpec.builder("sanitizedToString")
        .addAnnotation(suppressAnnotation)
        .receiver(ClassName.bestGuess(classData.name))
        .addModifiers(KModifier.INLINE, KModifier.INTERNAL)
        .returns(String::class)
        .apply {
          if (isDebug) {
            addStatement("return %P", "${classData.className}($debugString)")
          }
          else {
            addCode(block)
          }
        }
        .build()
  }
}
