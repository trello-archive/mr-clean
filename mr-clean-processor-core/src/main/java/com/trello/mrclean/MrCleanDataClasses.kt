package com.trello.mrclean

import com.google.devtools.ksp.symbol.KSFile

data class MrCleanClassData(
    val qualifiedName: String,
    val simpleName: String,
    val qualifiedPackage: String,
    val enclosingPackage: String,
    val properties: List<MrCleanProperty>,
    val originatingFile: KSFile?,
) {
    fun getFileName() = "SanitizationFor$enclosingPackage${qualifiedName.removePrefix(qualifiedPackage)}"
}

data class MrCleanProperty(
    val name: String,
    val type: String? = null,
    val isPublic: Boolean,
)