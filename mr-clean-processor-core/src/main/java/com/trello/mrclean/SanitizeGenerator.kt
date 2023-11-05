package com.trello.mrclean

import com.squareup.kotlinpoet.*

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
        println("debug string is $debugString")
        val sanitizedOutput = mapOf(
            "className" to simpleClassName,
//            "hexString" to Int::class.java.asTypeName(),
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
        println(build.toString())
        return build
    }
}
