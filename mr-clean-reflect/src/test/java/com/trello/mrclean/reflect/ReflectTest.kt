package com.trello.mrclean.reflect

import org.junit.Assert.assertEquals
import org.junit.Test

class ReflectTest {
  data class OneStringParam(val stringParam: String)
  data class OneIntParam(val intParam: Int)
  data class OneBooleanParam(val booleanParam: Boolean)
  data class TwoPrimitiveParams(val stringParam: String, val intParam: Int)
  data class TwoComplexParams(val oneStringParam: OneStringParam, val oneIntParam: OneIntParam)
  data class ThreeComplexParams(val oneStringParam: OneStringParam, val oneIntParam: OneIntParam, val oneBooleanParam: OneBooleanParam)
  data class NestedComplexParams(val threeComplexParams1: ThreeComplexParams, val threeComplexParams2: ThreeComplexParams)

  @Test
  fun testReflectOneStringParam() {
    assertEquals("{ stringParam = Meow }", OneStringParam("Meow").sanitizedToString())
  }

  @Test
  fun testReflectOneIntParam() {
    assertEquals("{ intParam = 1 }", OneIntParam(1).sanitizedToString())
  }

  @Test
  fun testReflectOneBooleanParam() {
    assertEquals("{ booleanParam = false }",
        OneBooleanParam(false).sanitizedToString())
  }

  @Test
  fun testReflectTwoPrimitiveParam() {
    assertEquals("{ intParam = 2, stringParam = Meow }",
        TwoPrimitiveParams("Meow", 2).sanitizedToString())
  }

  @Test
  fun testReflectTwoComplexParam() {
    assertEquals("{ oneIntParam = OneIntParam(intParam=1), oneStringParam = OneStringParam(stringParam=Meow) }",
        TwoComplexParams(OneStringParam("Meow"), OneIntParam(1)).sanitizedToString())
  }

  @Test
  fun testReflectThreeComplexParams() {
    assertEquals("{ oneBooleanParam = OneBooleanParam(booleanParam=false), oneIntParam = OneIntParam(intParam=1), oneStringParam = OneStringParam(stringParam=Meow) }",
        ThreeComplexParams(OneStringParam("Meow"), OneIntParam(1), OneBooleanParam(false)).sanitizedToString())
  }

  @Test
  fun testReflectNestedComplexParams() {
    assertEquals("{ threeComplexParams1 = ThreeComplexParams(oneStringParam=OneStringParam(stringParam=Meow), oneIntParam=OneIntParam(intParam=1), oneBooleanParam=OneBooleanParam(booleanParam=true)), threeComplexParams2 = ThreeComplexParams(oneStringParam=OneStringParam(stringParam=bark), oneIntParam=OneIntParam(intParam=2), oneBooleanParam=OneBooleanParam(booleanParam=false)) }",
        NestedComplexParams(
            ThreeComplexParams(OneStringParam("Meow"), OneIntParam(1), OneBooleanParam(true)),
            ThreeComplexParams(OneStringParam("bark"), OneIntParam(2), OneBooleanParam(false))
        ).sanitizedToString())
  }
}