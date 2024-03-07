package com.trello.mrclean

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName

internal object SanitizeGenerator {
    fun generateSanitizedToString(
        className: ClassName,
        qualifiedClassName: String,
        simpleClassName: String,
        properties: List<MrCleanProperty>,
        classTypes: List<TypeVariableName>,
        isDebug: Boolean,
    ): FunSpec {
        val parameteizedClassName = if (classTypes.isEmpty()) {
            className
        } else {
            className.parameterizedBy(classTypes)
        }
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
            .receiver(parameteizedClassName)
            .addTypeVariables(classTypes.map { TypeVariableName(it.name, it.bounds, null) })
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
