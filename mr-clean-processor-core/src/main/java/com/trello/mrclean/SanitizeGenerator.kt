package com.trello.mrclean

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

internal object SanitizeGenerator {
    fun generateSanitizedToString(
        qualifiedClassName: String,
        simpleClassName: String,
        properties: List<MrCleanProperty>,
        isDebug: Boolean,
    ): FunSpec {
        val debugString = properties.joinToString {
            if (it.isPublic) "${it.name} = ${"$"}${it.name}" else "${it.name} = <private>"
        }
        val sanitizedOutput = mapOf(
            "className" to simpleClassName,
        )
        val suppressAnnotation = AnnotationSpec.builder(Suppress::class)
            .addMember("%S", "NOTHING_TO_INLINE")
            .build()
        val block = CodeBlock.builder()
            .addNamed(
                "return \"%className:L@\${hashCode().toString(16)}\"\n",
                sanitizedOutput,
            )
            .build()
        val build = FunSpec.builder("sanitizedToString")
            .addAnnotation(suppressAnnotation)
            .receiver(ClassName.bestGuess(qualifiedClassName))
            .addModifiers(KModifier.INLINE, KModifier.INTERNAL)
            .returns(String::class)
            .apply {
                if (isDebug) {
                    addStatement("return %P", "$simpleClassName($debugString)")
                } else {
                    addCode(block)
                }
            }
            .build()
        return build
    }
}
