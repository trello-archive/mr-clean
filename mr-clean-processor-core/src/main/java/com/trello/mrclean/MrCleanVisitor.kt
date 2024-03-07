package com.trello.mrclean

import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver

class MrCleanVisitor(
    private val logger: KSPLogger,
) : KSVisitorVoid() {
    val properties = mutableListOf<MrCleanProperty>()
    val typeInfo = mutableListOf<TypeVariableName>()
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val properties: Sequence<KSPropertyDeclaration> = classDeclaration.getAllProperties()
            .filter { it.validate() }

        properties.forEach {
            visitPropertyDeclaration(it, Unit)
        }
        val classTypeParams = classDeclaration.typeParameters.toTypeParameterResolver()
        typeInfo.addAll(classTypeParams.parametersMap.values)
    }

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        val propertyName = property.simpleName.asString()
        val isPublic = property.isPublic()

        val resolvedType: KSType = property.type.resolve()
        val type = resolvedType.declaration.qualifiedName?.asString()
        properties += MrCleanProperty(propertyName, type, isPublic)
    }
}
