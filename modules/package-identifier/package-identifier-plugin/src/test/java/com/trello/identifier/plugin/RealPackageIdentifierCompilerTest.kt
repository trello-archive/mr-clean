package com.trello.identifier.plugin

import org.junit.Assert
import org.junit.Test

class RealPackageIdentifierCompilerTest {

  @Test
  fun testCompileDebuggable() {
    val compiler: PackageIdentifierCompiler = RealPackageIdentifierCompiler()

    val packageName = "com.trello.sample"
    val output = StringBuilder()
    val isDebuggable = false

    compiler.createPackageIdentifierFile(packageName, output, isDebuggable)

    val expectedOutput = """
     |// Generated, do not modify!
     |package com.trello.sample
     |
     |import com.trello.identifier.annotation.PackageId
     |
     |@PackageId(isDebug = false)
     |class PackageIdentifier
     |
    """.trimMargin("|")
    Assert.assertEquals(expectedOutput, output.toString())
  }

  @Test
  fun testCompileNotDebuggable() {
    val compiler: PackageIdentifierCompiler = RealPackageIdentifierCompiler()

    val packageName = "com.trello.sample"
    val output = StringBuilder()
    val isDebuggable = true

    compiler.createPackageIdentifierFile(packageName, output, isDebuggable)

    val expectedOutput = """
     |// Generated, do not modify!
     |package com.trello.sample
     |
     |import com.trello.identifier.annotation.PackageId
     |
     |@PackageId(isDebug = true)
     |class PackageIdentifier
     |
    """.trimMargin("|")
    Assert.assertEquals(expectedOutput, output.toString())
  }
}