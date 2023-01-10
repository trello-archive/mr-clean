package com.trello.mrclean.plugin

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.manifest.parseManifest
import com.android.builder.errors.EvalIssueException
import com.android.builder.errors.IssueReporter
import com.trello.mrclean.VERSION
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
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
        project.plugins.apply("kotlin-kapt")

        project.plugins.all {
            when (it) {
                is LibraryPlugin -> {
                    addKaptDeps(project)
                    project.afterEvaluate {
                        val packageName = getPackageNameBase(project.extensions.getByType(BaseExtension::class.java))
                        project.extensions[LibraryExtension::class].run {
                            configureSanitizationGeneration(project, libraryVariants, packageName)
                        }
                    }
                }

                is AppPlugin -> {
                    addKaptDeps(project)
                    project.afterEvaluate {
                        val packageName = getPackageNameBase(project.extensions.getByType(BaseExtension::class.java))
                        project.extensions[AppExtension::class].run {
                            configureSanitizationGeneration(project, applicationVariants, packageName)
                        }
                    }
                }
            }
        }
    }

    private fun addKaptDeps(project: Project) {
        val implDeps = project.configurations.getByName("implementation").dependencies
        implDeps.add(project.dependencies.create("com.trello.mrclean:mr-clean-annotations:$VERSION"))
        val kaptDeps = project.configurations.getByName("kapt").dependencies
        kaptDeps.add(project.dependencies.create("com.trello.mrclean:mr-clean-processor:$VERSION"))
    }

    private fun getPackageNameBase(extension: BaseExtension): String {
        return if (extension.namespace != null) {
            log.debug("using namespace ${extension.namespace}")
            extension.namespace!!
        } else {
            log.debug("couldn't find a namespace")
            extension.sourceSets
                .map { it.manifest.srcFile }
                .filter { it.exists() }
                .map { getPackageFromManifest(it) }
                .filterNotNull()
                .forEach {
                    log.debug("using package $it")
                    return it
                }

            throw IllegalStateException("MrClean: Couldn't find namespace or package in manifest, aborting.")
        }
    }

    private fun getPackageFromManifest(it: File) = parseManifest(
        it,
        true,
        BooleanSupplier { true },
        object : IssueReporter() {
            override fun hasIssue(type: Type) = false
            override fun reportIssue(type: Type, severity: Severity, exception: EvalIssueException) =
                throw exception
        }).packageName


    private fun configureSanitizationGeneration(
        project: Project,
        variants: DomainObjectSet<out BaseVariant>,
        packageName: String
    ) {
        variants.all { variant ->
            val once = AtomicBoolean()

            // apply APT options for use in MrCleanProcessor
            variant.javaCompileOptions.annotationProcessorOptions.arguments["mrclean.packagename"] = packageName
            variant.javaCompileOptions.annotationProcessorOptions.arguments["mrclean.debug"] =
                variant.buildType.isDebuggable.toString()

            variant.outputs.all { _ ->
                if (once.compareAndSet(false, true)) {
                    val taskName = "generate${variant.name.capitalize()}RootSanitizeFunction"
                    val outputDir = project.buildDir.resolve("generated/source/mrclean/${variant.name}")
                    val task = project.tasks
                        .create(taskName, GenerateRootFunctions::class.java) {
                            it.outputDir = outputDir
                            it.packageName = packageName
                            variant.registerJavaGeneratingTask(it, outputDir)
                            variant.addJavaSourceFoldersToModel(outputDir)
                        }

                    project.files().builtBy(task)
                }
            }
        }
    }
}

private operator fun <T : Any> ExtensionContainer.get(type: KClass<T>): T {
    return getByType(type.java)
}
