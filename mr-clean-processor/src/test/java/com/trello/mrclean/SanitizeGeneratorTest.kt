package com.trello.mrclean

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.metadata.Flag
import kotlinx.metadata.flagsOf
import org.junit.Test

import org.junit.Assert.*

class SanitizeGeneratorTest {

  data class TwoParam(val bar: Int, val meow: Int)

  @Test
  fun generateDebugSanitizedToString() {
    val propertyData = listOf(
        PropertyData(flagsOf(Flag.IS_PUBLIC), "bar", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "meow", INT)
    )
    val classData = ClassData(TwoParam::class.java.canonicalName, propertyData)
    val expectedOuput = """
      |package com.example

      |import com.trello.mrclean.SanitizeGeneratorTest
      |import kotlin.String
      |import kotlin.Suppress

      |@Suppress("NOTHING_TO_INLINE")
      |inline fun SanitizeGeneratorTest.TwoParam.sanitizedToString(): String =
      |        "TwoParam(bar = ${"$"}bar, meow = ${"$"}meow)"
      |
      """.trimMargin()

    assertEquals(expectedOuput, buildFile(SanitizeGenerator.generateSanitizedToString(classData, true)))

  }

  @Test
  fun generateReleaseSanitizedToString() {
    val propertyData = listOf(
        PropertyData(flagsOf(Flag.IS_PUBLIC), "bar", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "meow", INT)
    )
    val classData = ClassData(TwoParam::class.java.canonicalName, propertyData)
    val expectedOuput = """
      |package com.example

      |import com.trello.mrclean.SanitizeGeneratorTest
      |import java.lang.Integer
      |import kotlin.String
      |import kotlin.Suppress

      |@Suppress("NOTHING_TO_INLINE")
      |inline fun SanitizeGeneratorTest.TwoParam.sanitizedToString(): String =
      |        "TwoParam@${"$"}{Integer.toHexString(hashCode())}"
      |
      """.trimMargin()

    assertEquals(expectedOuput, buildFile(SanitizeGenerator.generateSanitizedToString(classData, false)))

  }

  data class EightParam(
      val one: Int,
      val two: Int,
      val three: Int,
      val four: Int,
      val five: Int,
      val six: Int,
      val seven: Int,
      val eight: Int
  )

  @Test
  fun generateReleaseSanitizedToStringForLongClassHeader() {
    val propertyData = listOf(
        PropertyData(flagsOf(Flag.IS_PUBLIC), "one", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "two", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "three", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "four", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "five", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "six", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "seven", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "eight", INT)
    )

    val classData = ClassData(EightParam::class.java.canonicalName, propertyData)
    val expectedOuput = """
      |package com.example

      |import com.trello.mrclean.SanitizeGeneratorTest
      |import java.lang.Integer
      |import kotlin.String
      |import kotlin.Suppress

      |@Suppress("NOTHING_TO_INLINE")
      |inline fun SanitizeGeneratorTest.EightParam.sanitizedToString(): String =
      |        "EightParam@${"$"}{Integer.toHexString(hashCode())}"
      |
      """.trimMargin()
    val output = buildFile(SanitizeGenerator.generateSanitizedToString(classData, false))

    assertEquals(expectedOuput, output)
  }

  @Test
  fun generateDebugSanitizedToStringForLongClassHeader() {
    val propertyData = listOf(
        PropertyData(flagsOf(Flag.IS_PUBLIC), "one", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "two", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "three", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "four", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "five", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "six", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "seven", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "eight", INT)
    )

    val classData = ClassData(EightParam::class.java.canonicalName, propertyData)
    val expectedOuput = """
      |package com.example

      |import com.trello.mrclean.SanitizeGeneratorTest
      |import kotlin.String
      |import kotlin.Suppress

      |@Suppress("NOTHING_TO_INLINE")
      |inline fun SanitizeGeneratorTest.EightParam.sanitizedToString(): String =
      |        "EightParam(one = ${"$"}one, two = ${"$"}two, three = ${"$"}three, four = ${"$"}four, five = ${"$"}five, six = ${"$"}six, seven = ${"$"}seven, eight = ${"$"}eight)"
      |
      """.trimMargin()
    val output = buildFile(SanitizeGenerator.generateSanitizedToString(classData, true))

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
}