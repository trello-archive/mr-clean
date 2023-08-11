package com.trello.mrclean.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.*
import com.android.build.gradle.internal.manifest.parseManifest
import com.android.builder.errors.EvalIssueException
import com.android.builder.errors.IssueReporter
import com.trello.mrclean.VERSION
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
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
        project.plugins.apply("kotlin-kapt")
        addKaptDeps(project)
        val baseExtension = project.extensions.getByType(BaseExtension::class.java)
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val packageName = getPackageNameBase(baseExtension)
            variant.javaCompilation.annotationProcessor.arguments.put("mrclean.packagename", packageName)
            variant.javaCompilation.annotationProcessor.arguments.put(
                "mrclean.debug",
                (variant.buildType == "debug").toString()
            )
            val taskName = "generate${variant.name.capitalize()}RootSanitizeFunction"
            val outputDir = project.buildDir.resolve("generated/source/mrclean/${variant.name}")
            log.debug("MrClean: task $taskName using directory $outputDir")
            val task = project.tasks.register(taskName, GenerateRootFunctions::class.java) {
                it.outputDir.set(outputDir)
                it.packageName.set(packageName)

            }
            variant.sources.assets?.addGeneratedSourceDirectory(task, GenerateRootFunctions::outputDir)
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

}

private operator fun <T : Any> ExtensionContainer.get(type: KClass<T>): T {
    return getByType(type.java)
}
