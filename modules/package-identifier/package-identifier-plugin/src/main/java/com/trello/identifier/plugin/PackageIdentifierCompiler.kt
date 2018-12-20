package com.trello.identifier.plugin

interface PackageIdentifierCompiler {
  fun createPackageIdentifierFile(packageName: String, output: Appendable, isDebuggable: Boolean)
}