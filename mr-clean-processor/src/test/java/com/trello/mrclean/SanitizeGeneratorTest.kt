package com.trello.mrclean

import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.metadata.Flag
import kotlinx.metadata.flagsOf
import org.junit.Test

import org.junit.Assert.*

class SanitizeGeneratorTest {

  data class Foo(val bar: Int, val meow: Int)

  @Test
  fun generateDebugSanitizedToString() {
    val propertyData = listOf(
        PropertyData(flagsOf(Flag.IS_PUBLIC), "bar", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "meow", INT)
    )
    val classData = ClassData(Foo::class.java.canonicalName, propertyData)
    val expectedOuput = """
      |@kotlin.Suppress("NOTHING_TO_INLINE")
      |inline fun com.trello.mrclean.SanitizeGeneratorTest.Foo.sanitizedToString(): kotlin.String = "Foo(bar = ${"$"}bar, meow = ${"$"}meow)"
""".trimMargin()

    assertEquals(expectedOuput, SanitizeGenerator.generateSanitizedToString(classData, true).toString())

  }

  @Test
  fun generateReleaseSanitizedToString() {
    val propertyData = listOf(
        PropertyData(flagsOf(Flag.IS_PUBLIC), "bar", INT),
        PropertyData(flagsOf(Flag.IS_PUBLIC), "meow", INT)
    )
    val classData = ClassData(Foo::class.java.canonicalName, propertyData)
    val expectedOuput = """
      |@kotlin.Suppress("NOTHING_TO_INLINE")
      |inline fun com.trello.mrclean.SanitizeGeneratorTest.Foo.sanitizedToString(): kotlin.String = "Foo@${"$"}{java.lang.Integer.toHexString(hashCode())}"
      |
""".trimMargin()

    assertEquals(expectedOuput, SanitizeGenerator.generateSanitizedToString(classData, false).toString())

  }
}