package com.trello.mrclean.plugin

import org.junit.Test

import org.junit.Assert.*

class RootFunctionGeneratorTest {

  @Test
  fun createProdRootFunction() {
    val generator = RootFunctionGenerator()
    val output = StringBuilder()
    generator.createProdRootFunction("com.example").writeTo(output)

    val expectedOutput = """
      |// This is the root function that generated functions will overload
      |package com.example

      |import kotlin.Any
      |import kotlin.String

      |internal fun Any.sanitizedToString(): String =
      |        error("No function generated! Make sure to annotate with @Sanitize")
      |
    """.trimMargin()
    assertEquals(expectedOutput, output.toString())
  }

  @Test
  fun createDebugRootFunction() {
    val generator = RootFunctionGenerator()
    val output = StringBuilder()
    generator.createDebugRootFunction("com.example").writeTo(output)

    val expectedOutput = """
      |// This is the root function that generated functions will overload
      |package com.example

      |import kotlin.Any
      |import kotlin.String

      |internal fun Any.sanitizedToString(): String = toString()
      |
    """.trimMargin()
    assertEquals(expectedOutput, output.toString())
  }
}