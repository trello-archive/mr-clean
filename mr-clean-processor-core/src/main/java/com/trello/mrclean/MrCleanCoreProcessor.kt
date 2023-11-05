package com.trello.mrclean

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.writeTo
import com.trello.mrclean.annotations.Sanitize

class MrCleanCoreProcessor(
    val codeGenerator: CodeGenerator,
    val options: Map<String, String>,
    private val logger: KSPLogger,
) {
    fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("Mr. Clean Core Processor has $options")

        val packageName = options[PACKAGE_KEY]

        if (packageName == null) {
            logger.error("Mr. Clean didn't get a value for package name, a package must be passed in with the key \"mrclean.packagename\"")
            return emptyList()
        }
        val annotationName = Sanitize::class.qualifiedName!!
        val unfilteredSymbols = resolver.getSymbolsWithAnnotation(annotationName)
        val symbolsNotProcessed = unfilteredSymbols.filterNot { it.validate(symbolPredicate) }.toList()
        val symbols = unfilteredSymbols.filterIsInstance<KSClassDeclaration>().filter { it.validate(symbolPredicate) }

        if (!symbols.iterator().hasNext()) {
            logger.info("Mr. Clean found no symbols to process, exiting")
            return symbolsNotProcessed
        }

        val rootFromOpts = options[ROOT_GENERATOR_KEY]
        val debugFromOpts = options[DEBUG_KEY]

        if (rootFromOpts == null) {
            logger.info("Mr. Clean defaulting to not generating a root function. Can overrider with \"$ROOT_GENERATOR_KEY\"")
        }

        if (debugFromOpts == null) {
            logger.info("Mr. Clean defaulting to sanitization. Can be overriden with \"$DEBUG_KEY\"")
        }

        val isDebug = debugFromOpts?.toBoolean() ?: false
        val generateRoot = options[ROOT_GENERATOR_KEY]?.toBoolean() ?: false

        var symbolCount = 0
        val classes = mutableListOf<MrCleanClassData>()
        symbols.forEach {
            val visitor = MrCleanVisitor(logger)
            it.accept(visitor, Unit)
            classes.add(
                MrCleanClassData(
                    qualifiedName = it.qualifiedName?.asString()!!,
                    simpleName = it.simpleName.asString(),
                    properties = visitor.properties,
                    enclosingPackage = it.packageName.getShortName()
                        .replaceFirstChar { first -> first.uppercase() },
                    qualifiedPackage = it.packageName.asString(),
                    originatingFile = it.containingFile,
                ),
            )
            symbolCount++
        }

        val map = classes.map {
            it to SanitizeGenerator.generateSanitizedToString(
                it.qualifiedName,
                it.simpleName,
                it.properties,
                isDebug,
            )
        }
        map.map { (classData, funSpec) ->
            val fileSpec = FileSpec.builder(
                packageName,
                classData.getFileName(),
            )
                .addFunction(funSpec)
                .build()

            val originatingFiles = if (classData.originatingFile == null) {
                emptyList()
            } else {
                listOf(classData.originatingFile)
            }
            fileSpec.writeTo(
                codeGenerator = codeGenerator,
                aggregating = false,
                originatingKSFiles = originatingFiles,
            )
        }
        if (generateRoot) {
            logger.info("Mr. Clean generating root function Any.santizedToString()")
            createRootFunction(packageName).writeTo(codeGenerator, false)
        }
        logger.info("Mr. Clean processed $symbolCount symbols returning ${symbolsNotProcessed.size}")
        return symbolsNotProcessed
    }

    private val symbolPredicate: (data: KSNode?, declaration: KSNode) -> Boolean =
        { data, declaration ->
            when (declaration) {
                // we only care about class, annotation and property
                is KSClassDeclaration,
                is KSAnnotation,
                is KSPropertyDeclaration,
                -> {
                    true
                }
                // only care about types that belong to parameters
                is KSTypeReference -> data is KSPropertyDeclaration
                else -> {
                    false
                }
            }
        }

    private fun createRootFunction(packageName: String): FileSpec {
        val rootFunction = FunSpec.builder("sanitizedToString")
            .addModifiers(KModifier.INTERNAL)
            .receiver(Any::class)
            .returns(String::class)
            .addStatement(
                "return error(%S)",
                "No function generated! Make sure to annotate with @Sanitize",
            )
            .build()
        return FileSpec.builder(packageName, "RootSanitizeFunction")
            .addFunction(rootFunction)
            .addFileComment("This is the root function that generated functions will overload")
            .build()
    }

    companion object {
        const val ROOT_GENERATOR_KEY = "mrclean.rootgenerator"
        const val DEBUG_KEY = "mrclean.debug"
        const val PACKAGE_KEY = "mrclean.packagename"
    }
}
