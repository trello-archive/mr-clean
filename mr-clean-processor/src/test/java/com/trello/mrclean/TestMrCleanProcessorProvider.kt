package com.trello.mrclean

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class TestMrCleanProcessorProvider(val defaultOpts: Map<String, String>) : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        MrCleanProcessor(
            codeGenerator = environment.codeGenerator,
            options = environment.options + defaultOpts,
            logger = environment.logger,
        )
}
