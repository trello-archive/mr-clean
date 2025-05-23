package com.trello.mrclean.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.manifest.parseManifest
import com.android.builder.errors.EvalIssueException
import com.android.builder.errors.IssueReporter
import com.google.devtools.ksp.gradle.KspExtension
import com.trello.mrclean.VERSION
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.slf4j.LoggerFactory
import java.io.File
import java.util.function.BooleanSupplier
import kotlin.reflect.KClass

/**
 * Based on butterknife's multi-module gradle plugin
 */
class MrCleanPlugin : Plugin<Project> {
    private val log by lazy {
        LoggerFactory.getLogger("Mr. Clean")
    }

    override fun apply(project: Project) {
        project.plugins.apply("com.google.devtools.ksp")
        val kspExtension = getOrCreateKsp(project, "ksp")
        addKspDeps(project)
        val baseExtension = project.extensions.getByType(BaseExtension::class.java)
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        val buildTypeSet = mutableSetOf<String>()
        // we are generating the root here, so we don't need to generate it in the processor
        kspExtension.arg("mrclean.rootgenerator", "false")
        androidComponents.onVariants { variant ->
            val buildType = variant.buildType
            if (buildType != null && !buildTypeSet.contains(buildType)) {
                val isDebug = (variant.buildType == "debug")
                val cleaned = buildType.replaceFirstChar { it.uppercaseChar() }
                addMrCleanProcessor(project, cleaned, isDebug)
                buildTypeSet.add(buildType)
            }
            val manifestParsingAllowedProvider = project.provider { true }
            val packageName = getPackageNameBase(baseExtension, manifestParsingAllowedProvider)
            kspExtension.arg("mrclean.packagename", packageName)
            val taskName = "generate${variant.name.capitalize()}RootSanitizeFunction"
            val outputDir = project.buildDir.resolve("generated/source/mrclean/${variant.name}")
            log.debug("MrClean: task $taskName using directory $outputDir")
            val task = project.tasks.register(taskName, GenerateRootFunctions::class.java) {
                it.outputDir.set(outputDir)
                it.packageName.set(packageName)
            }
            log.debug("MrClean: setting ${variant.sources.java} to ${GenerateRootFunctions::outputDir}")
            variant.sources.java?.addGeneratedSourceDirectory(
                task,
                GenerateRootFunctions::outputDir,
            )
        }
    }

    private fun getOrCreateKsp(project: Project, name: String): KspExtension {
        val hasKsp = project.extensions.findByName(name) != null
        return if (hasKsp) {
            log.debug("MrClean: found $name")
            project.extensions.getByName(name) as KspExtension
        } else {
            log.debug("MrClean: trying to create $name")
            project.extensions.create(name, KspExtension::class.java)
        }
    }

    private fun addKspDeps(project: Project) {
        val implDeps = project.configurations.getByName("implementation").dependencies
        implDeps.add(project.dependencies.create("com.trello.mrclean:mr-clean-runtime:$VERSION"))
    }

    private fun addMrCleanProcessor(project: Project, configuration: String, isDebug: Boolean) {
        val coordinates = if (isDebug) {
            "com.trello.mrclean:mr-clean-debug-processor"
        } else {
            "com.trello.mrclean:mr-clean-processor"
        }
        log.debug("Mr Clean is adding: ksp$configuration $coordinates:$VERSION")
        val kspDep = project.configurations.getByName("ksp$configuration").dependencies
        kspDep.add(project.dependencies.create("$coordinates:$VERSION"))
    }

    private fun getPackageNameBase(
        extension: BaseExtension,
        manifestParsingAllowedProvider: Provider<Boolean>? = null
    ): String {
        return if (extension.namespace != null) {
            log.debug("using namespace ${extension.namespace}")
            extension.namespace!!
        } else {
            log.debug("couldn't find a namespace")
            extension.sourceSets
                .map { it.manifest.srcFile }
                .filter { it.exists() }
                .mapNotNull { getPackageFromManifest(it, manifestParsingAllowedProvider) }
                .forEach {
                    log.debug("using package $it")
                    return it
                }

            throw IllegalStateException("MrClean: Couldn't find namespace or package in manifest, aborting.")
        }
    }

    private fun getPackageFromManifest(
        it: File,
        manifestParsingAllowedProvider: Provider<Boolean>? = null
    ) = parseManifest(
        manifestFileContent = it.readText(),
        manifestFilePath = it.path,
        manifestFileRequired = true,
        manifestParsingAllowedProvider = manifestParsingAllowedProvider,
        issueReporter = object : IssueReporter() {
            override fun hasIssue(type: Type) = false
            override fun reportIssue(
                type: Type,
                severity: Severity,
                exception: EvalIssueException,
            ) =
                throw exception
        },
    ).packageName
}

private operator fun <T : Any> ExtensionContainer.get(type: KClass<T>): T {
    return getByType(type.java)
}
