package com.trello.mrclean

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*

class MrCleanProcessor(
    val codeGenerator: CodeGenerator,
    val options: Map<String, String>,
    private val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        return MrCleanCoreProcessor(
            codeGenerator = codeGenerator,
            options = options,
            logger = logger,
        ).process(resolver)
    }
}