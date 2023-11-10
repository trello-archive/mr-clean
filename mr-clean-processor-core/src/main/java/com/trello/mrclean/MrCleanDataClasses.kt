package com.trello.mrclean

import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeVariableName

data class MrCleanClassData(
    val className: ClassName,
    val qualifiedName: String,
    val simpleName: String,
    val qualifiedPackage: String,
    val enclosingPackage: String,
    val properties: List<MrCleanProperty>,
    val classTypes: List<TypeVariableName>,
    val originatingFile: KSFile?,
) {
    fun getFileName() =
        "SanitizationFor$enclosingPackage${qualifiedName.removePrefix(qualifiedPackage)}"
}

data class MrCleanProperty(
    val name: String,
    val type: String? = null,
    val isPublic: Boolean,
)
