package com.trello.mrclean

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

@AutoService(SymbolProcessorProvider::class)
class MrCleanDebugProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        MrCleanDebugProcessor(
            codeGenerator = environment.codeGenerator,
            options = environment.options,
            logger = environment.logger,
        )
}
