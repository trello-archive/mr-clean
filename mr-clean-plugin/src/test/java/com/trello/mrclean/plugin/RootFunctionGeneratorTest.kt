package com.trello.mrclean.plugin

import org.junit.Test

import org.junit.Assert.*

class RootFunctionGeneratorTest {

  @Test
  fun createRootFunctionWithoutReflection() {
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

  @Test
  fun createRootFunctionWithReflection() {
    val generator = RootFunctionGenerator()
    val output = StringBuilder()
    generator.createRootFunction("com.example", true).writeTo(output)

    val expectedOutput = """
      |// This is the root function that generated functions will overload
      |package com.example

      |import com.trello.mrclean.reflect.reflectedToString
      |import kotlin.Any
      |import kotlin.String

      |internal fun Any.sanitizedToString(): String = reflectedToString()
      |
    """.trimMargin()
    assertEquals(expectedOutput, output.toString())
  }
}