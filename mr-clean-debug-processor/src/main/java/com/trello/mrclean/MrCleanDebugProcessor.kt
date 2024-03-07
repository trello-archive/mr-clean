package com.trello.mrclean

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated


class MrCleanDebugProcessor(
    val codeGenerator: CodeGenerator,
    val options: Map<String, String>,
    private val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("Mr. Clean Debug Processor has $options")
        val debugOptions = mutableMapOf<String, String>()
        debugOptions.putAll(options)
        if (options[MrCleanCoreProcessor.DEBUG_KEY] == null) {
            logger.info("\"MrCleanCoreProcessor.DEBUG_KEY\" not set, defatuling to true")
            debugOptions[MrCleanCoreProcessor.DEBUG_KEY] = "true"
        }
        if (options[MrCleanCoreProcessor.ROOT_GENERATOR_KEY] == null) {
            logger.info("\"$MrCleanCoreProcessor.ROOT_GENERATOR_KEY\" not set, defaulting to true")
            debugOptions[MrCleanCoreProcessor.ROOT_GENERATOR_KEY] = "true"
        }
        return MrCleanCoreProcessor(
            codeGenerator = codeGenerator,
            options = debugOptions,
            logger = logger,
        ).process(resolver)
    }
}
