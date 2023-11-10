package com.trello.mrclean

import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCompilerApi::class)
class MrCleanProcessorTest {
    @Test
    fun `default sanitizes string`() {
        val kotlinSource = SourceFile.kotlin(
            "KClass.kt",
            """
            package com.test
            import com.trello.mrclean.annotations.Sanitize

            @Sanitize
            data class TwoParam(val bar: Int, val meow: Int)
    """,
        )
        val option = mapOf("mrclean.packagename" to "com.test")
        val compilation = KotlinCompilation().apply {
            sources = listOf(kotlinSource)

            // pass your own instance of an annotation processor
            symbolProcessorProviders = listOf(TestMrCleanProcessorProvider(option))

            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }
        val result = compilation.compile()
        assertThat(result.exitCode).isEqualTo(ExitCode.OK)
        val generatedSourcesDir = compilation.kspSourcesDir

        val generatedFile =
            File(generatedSourcesDir, "kotlin/com/test/SanitizationForTest.TwoParam.kt")
        assertThat(generatedFile.exists()).isTrue()
        assertThat(
            generatedFile.readText().trimIndent(),
        ).isEqualTo(
            """
package com.test

import kotlin.String
import kotlin.Suppress

@Suppress("NOTHING_TO_INLINE")
internal inline fun TwoParam.sanitizedToString(): String = "TwoParam@${'$'}{hashCode().toString(16)}"
            """.trimIndent(),
        )
    }

    @Test
    fun `default sanitizes private string`() {
        val kotlinSource = SourceFile.kotlin(
            "KClass.kt",
            """
            package com.test
            import com.trello.mrclean.annotations.Sanitize

            @Sanitize
            data class TwoParam(private val bar: Int, val meow: Int)
    """,
        )
        val option = mapOf(
            "mrclean.packagename" to "com.test",
            "mrclean.debug" to "true",
        )
        val compilation = KotlinCompilation().apply {
            sources = listOf(kotlinSource)

            // pass your own instance of an annotation processor
            symbolProcessorProviders = listOf(TestMrCleanProcessorProvider(option))

            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }
        val result = compilation.compile()
        assertThat(result.exitCode).isEqualTo(ExitCode.OK)
        val generatedSourcesDir = compilation.kspSourcesDir
        println(generatedSourcesDir)

        val generatedFile =
            File(generatedSourcesDir, "kotlin/com/test/SanitizationForTest.TwoParam.kt")
        assertThat(generatedFile.exists()).isTrue()
        println(generatedFile.readText())
        assertThat(
            generatedFile.readText().trimIndent(),
        ).isEqualTo(
            """
package com.test

import kotlin.String
import kotlin.Suppress

@Suppress("NOTHING_TO_INLINE")
internal inline fun TwoParam.sanitizedToString(): String =
    ""${'"'}TwoParam(bar = <private>, meow = ${'$'}meow)""${'"'}   
            """.trimIndent().trim(),
        )
        println(result)
    }

    @Test
    fun `root function generates`() {
        val kotlinSource = SourceFile.kotlin(
            "KClass.kt",
            """
            package com.test
            import com.trello.mrclean.annotations.Sanitize

            @Sanitize
            data class TwoParam(private val bar: Int, val meow: Int)
    """,
        )
        val option = mapOf(
            "mrclean.packagename" to "com.test",
            "mrclean.rootgenerator" to "true",
        )
        val compilation = KotlinCompilation().apply {
            sources = listOf(kotlinSource)

            // pass your own instance of an annotation processor
            symbolProcessorProviders = listOf(TestMrCleanProcessorProvider(option))

            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }
        val result = compilation.compile()
        assertThat(result.exitCode).isEqualTo(ExitCode.OK)
        val generatedSourcesDir = compilation.kspSourcesDir
        println(generatedSourcesDir)

        val generatedRootFile =
            File(generatedSourcesDir, "kotlin/com/test/RootSanitizeFunction.kt")

        assertThat(generatedRootFile.exists()).isTrue()
        assertThat(generatedRootFile.readText().trimIndent()).isEqualTo(
            """
// This is the root function that generated functions will overload
package com.test

import kotlin.Any
import kotlin.String

internal fun Any.sanitizedToString(): String =
    error("No function generated! Make sure to annotate with @Sanitize")
            """.trimIndent().trim(),
        )
    }
}
