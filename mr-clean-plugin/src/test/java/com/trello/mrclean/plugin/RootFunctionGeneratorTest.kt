package com.trello.mrclean.plugin

import org.junit.Test

import org.junit.Assert.*

class RootFunctionGeneratorTest {

  @Test
  fun createRootFunction() {
    val generator = RootFunctionGenerator()
    val output = StringBuilder()
    generator.createRootFunction("com.example", false).writeTo(output)

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
}