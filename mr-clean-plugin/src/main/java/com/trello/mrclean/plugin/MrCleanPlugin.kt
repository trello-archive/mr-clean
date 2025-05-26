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
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.dependencies
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Based on butterknife's multi-module gradle plugin
 */
class MrCleanPlugin : Plugin<Project> {
    private val log by lazy {
        LoggerFactory.getLogger("Mr. Clean")
    }

    override fun apply(project: Project) {
        project.pluginManager.apply("com.google.devtools.ksp")
        val kspExtension = getOrCreateKsp(project)
        addKspDeps(project)

        // we are generating the root here, so we don't need to generate it in the processor
        kspExtension.arg("mrclean.rootgenerator", "false")

        addSanitizationFunctions(project, kspExtension,)
    }

    fun addSanitizationFunctions(
        project: Project,
        kspExtension: KspExtension,
    ) {
        val baseExtension = project.extensions.getByType(BaseExtension::class.java)
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

        val manifestParsingAllowedProvider = project.provider { true }

        addProcessors(
            androidComponents,
            baseExtension,
            manifestParsingAllowedProvider,
            kspExtension,
            project
        )

        addRootFunctions(
            androidComponents,
            baseExtension,
            manifestParsingAllowedProvider,
            project
        )
    }

    private fun addProcessors(
        androidComponents: AndroidComponentsExtension<*, *, *>,
        baseExtension: BaseExtension,
        manifestParsingAllowedProvider: Provider<Boolean>,
        kspExtension: KspExtension,
        project: Project
    ) {
        // Must use beforeVariants, KSP processors are collected before onVariants is called
        androidComponents.beforeVariants { beforeVariant ->
            val buildTypeSet = mutableSetOf<String>()
            val packageName = getPackageNameBase(baseExtension, manifestParsingAllowedProvider)
            kspExtension.arg("mrclean.packagename", packageName)

            val buildType = beforeVariant.buildType
            if (buildType != null && !buildTypeSet.contains(buildType)) {
                addMrCleanProcessor(project, buildType)
                buildTypeSet.add(buildType)
            }
        }
    }

    private fun addRootFunctions(
        androidComponents: AndroidComponentsExtension<*, *, *>,
        baseExtension: BaseExtension,
        manifestParsingAllowedProvider: Provider<Boolean>,
        project: Project
    ) {
        // Must run in onVariants after KSP processors are applied
        androidComponents.onVariants { variant ->
            val packageName = getPackageNameBase(baseExtension, manifestParsingAllowedProvider)
            val cleanedVariant = variant.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
            val taskName = "generate${cleanedVariant}RootSanitizeFunction"
            val projectBuildDir = project.layout.buildDirectory.asFile.get()
            val outputDir =
                projectBuildDir.resolve("generated/source/mrclean/${variant.name}")

            log.debug("MrClean: task $taskName using directory $outputDir")
            val task = project.tasks.register(taskName, GenerateRootFunctions::class.java)
            task.configure {
                this.packageName.set(packageName)
                this.outputDir.set(outputDir)
            }

            log.debug("MrClean: setting ${variant.sources.java} to ${GenerateRootFunctions::outputDir}")
            variant.sources.java?.addGeneratedSourceDirectory(
                task,
                GenerateRootFunctions::outputDir,
            )
        }
    }

    private fun getOrCreateKsp(project: Project): KspExtension {
        val name = "ksp"
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
        project.dependencies {
            log.debug("Mr Clean is adding: ksp com.trello.mrclean:mr-clean-runtime:$VERSION")
            add("implementation", "com.trello.mrclean:mr-clean-runtime:$VERSION")
        }
    }

    private fun addMrCleanProcessor(project: Project, buildType: String) {
        val isDebug = (buildType == "debug")
        val configuration = buildType.replaceFirstChar { it.uppercaseChar() }

        val coordinates = if (isDebug) {
            "com.trello.mrclean:mr-clean-debug-processor"
        } else {
            "com.trello.mrclean:mr-clean-processor"
        }

        project.pluginManager.withPlugin("com.android.application") {
            log.debug("Mr Clean is adding: ksp$configuration $coordinates:$VERSION")
            project.dependencies {
                add("ksp$configuration", "$coordinates:$VERSION")
            }
        }
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

            throw IllegalStateException(
                "MrClean: Couldn't find namespace or package in manifest, aborting."
            )
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
