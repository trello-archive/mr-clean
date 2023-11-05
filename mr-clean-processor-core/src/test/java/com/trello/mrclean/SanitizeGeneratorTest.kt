package com.trello.mrclean
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import org.junit.Assert.*
import org.junit.Test

class SanitizeGeneratorTest {

    data class TwoParam(val bar: Int, val meow: Int)

    @Test
    fun generateDebugSanitizedToString() {
        val properties = listOf(
            MrCleanProperty(isPublic = true, name = "bar"),
            MrCleanProperty(isPublic = true, name = "meow"),
        )
        val qualifiedClassName = TwoParam::class.java.canonicalName
        val simpleClassName = TwoParam::class.java.simpleName
        val expectedOuput = """
      |package com.example

      |import com.trello.mrclean.SanitizeGeneratorTest
      |import kotlin.String
      |import kotlin.Suppress

      |@Suppress("NOTHING_TO_INLINE")
      |internal inline fun SanitizeGeneratorTest.TwoParam.sanitizedToString(): String =
      |    ${"\""}${"\""}${"\""}TwoParam(bar = ${"$"}bar, meow = ${"$"}meow)${"\""}${"\""}${"\""}
      |
        """.trimMargin()

        assertEquals(
            expectedOuput,
            buildFile(
                SanitizeGenerator.generateSanitizedToString(
                    qualifiedClassName,
                    simpleClassName,
                    properties,
                    true,
                ),
            ),
        )
    }

    @Test
    fun generateReleaseSanitizedToString() {
        val properties = listOf(
            MrCleanProperty(isPublic = true, name = "bar"),
            MrCleanProperty(isPublic = true, name = "meow"),
        )
        val qualifiedClassName = TwoParam::class.java.canonicalName
        val simpleClassName = TwoParam::class.java.simpleName
        val expectedOuput = """
      |package com.example

      |import com.trello.mrclean.SanitizeGeneratorTest
      |import kotlin.String
      |import kotlin.Suppress

      |@Suppress("NOTHING_TO_INLINE")
      |internal inline fun SanitizeGeneratorTest.TwoParam.sanitizedToString(): String =
      |    "TwoParam@${"$"}{hashCode().toString(16)}"
      |
        """.trimMargin()

        assertEquals(
            expectedOuput,
            buildFile(
                SanitizeGenerator.generateSanitizedToString(
                    qualifiedClassName,
                    simpleClassName,
                    properties,
                    false,
                ),
            ),
        )
    }

    data class EightParam(
        val one: Int,
        val two: Int,
        val three: Int,
        val four: Int,
        val five: Int,
        val six: Int,
        val seven: Int,
        val eight: Int,
    )

    @Test
    fun generateReleaseSanitizedToStringForLongClassHeader() {
        val properties = listOf(
            MrCleanProperty(isPublic = true, name = "one"),
            MrCleanProperty(isPublic = true, name = "two"),
            MrCleanProperty(isPublic = true, name = "three"),
            MrCleanProperty(isPublic = true, name = "four"),
            MrCleanProperty(isPublic = true, name = "five"),
            MrCleanProperty(isPublic = true, name = "six"),
            MrCleanProperty(isPublic = true, name = "seven"),
            MrCleanProperty(isPublic = true, name = "eight"),
        )
        val qualifiedClassName = EightParam::class.java.canonicalName
        val simpleClassName = EightParam::class.java.simpleName

        val expectedOuput = """
      |package com.example

      |import com.trello.mrclean.SanitizeGeneratorTest
      |import kotlin.String
      |import kotlin.Suppress

      |@Suppress("NOTHING_TO_INLINE")
      |internal inline fun SanitizeGeneratorTest.EightParam.sanitizedToString(): String =
      |    "EightParam@${"$"}{hashCode().toString(16)}"
      |
        """.trimMargin()
        val output = buildFile(
            SanitizeGenerator.generateSanitizedToString(
                qualifiedClassName,
                simpleClassName,
                properties,
                false,
            ),
        )

        assertEquals(expectedOuput, output)
    }

    @Test
    fun generateDebugSanitizedToStringForLongClassHeader() {
        val properties = listOf(
            MrCleanProperty(isPublic = true, name = "one"),
            MrCleanProperty(isPublic = true, name = "two"),
            MrCleanProperty(isPublic = true, name = "three"),
            MrCleanProperty(isPublic = true, name = "four"),
            MrCleanProperty(isPublic = true, name = "five"),
            MrCleanProperty(isPublic = true, name = "six"),
            MrCleanProperty(isPublic = true, name = "seven"),
            MrCleanProperty(isPublic = true, name = "eight"),
        )
        val qualifiedClassName = EightParam::class.java.canonicalName
        val simpleClassName = EightParam::class.java.simpleName

        val expectedOuput = """
      |package com.example

      |import com.trello.mrclean.SanitizeGeneratorTest
      |import kotlin.String
      |import kotlin.Suppress

      |@Suppress("NOTHING_TO_INLINE")
      |internal inline fun SanitizeGeneratorTest.EightParam.sanitizedToString(): String =
      |    ${"\""}${"\""}${"\""}EightParam(one = ${"$"}one, two = ${"$"}two, three = ${"$"}three, four = ${"$"}four, five = ${"$"}five, six = ${"$"}six, seven = ${"$"}seven, eight = ${"$"}eight)${"\""}${"\""}${"\""}
      |
        """.trimMargin()
        val output = buildFile(
            SanitizeGenerator.generateSanitizedToString(
                qualifiedClassName,
                simpleClassName,
                properties,
                true,
            ),
        )

        assertEquals(expectedOuput, output)
    }

    private fun buildFile(funSpec: FunSpec): String {
        val output = StringBuilder()
        FileSpec.builder("com.example", "Sanitizations")
            .addFunction(funSpec)
            .build()
            .writeTo(output)
        return output.toString()
    }

    @Test
    fun generateDebugSanitizedToStringIgnoresPrivateProperties() {
        val properties = listOf(
            MrCleanProperty(isPublic = false, name = "bar"),
            MrCleanProperty(isPublic = true, name = "meow"),
        )
        val qualifiedClassName = TwoParam::class.java.canonicalName
        val simpleClassName = TwoParam::class.java.simpleName
        val expectedOuput = """
       |package com.example

       |import com.trello.mrclean.SanitizeGeneratorTest
       |import kotlin.String
       |import kotlin.Suppress

       |@Suppress("NOTHING_TO_INLINE")
       |internal inline fun SanitizeGeneratorTest.TwoParam.sanitizedToString(): String =
       |    ${"\""}${"\""}${"\""}TwoParam(bar = <private>, meow = ${"$"}meow)${"\""}${"\""}${"\""}
       |
        """.trimMargin()
        assertEquals(
            expectedOuput,
            buildFile(
                SanitizeGenerator.generateSanitizedToString(
                    qualifiedClassName,
                    simpleClassName,
                    properties,
                    true,
                ),
            ),
        )
    }
}