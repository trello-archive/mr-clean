/*
 * Copyright (C) 2018 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trello.mrclean

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import kotlinx.metadata.Flag
import kotlinx.metadata.Flags
import kotlinx.metadata.KmClassVisitor
import kotlinx.metadata.KmConstructorVisitor
import kotlinx.metadata.KmPropertyVisitor
import kotlinx.metadata.KmTypeParameterVisitor
import kotlinx.metadata.KmTypeVisitor
import kotlinx.metadata.KmValueParameterVisitor
import kotlinx.metadata.KmVariance
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal fun Element.getClassHeader(): KotlinClassHeader? {
  return getAnnotation(Metadata::class.java)?.run {
    KotlinClassHeader(kind, metadataVersion, bytecodeVersion, data1, data2, extraString, packageName, extraInt)
  }
}

internal fun KotlinClassHeader.readKotlinClassMetadata(): KotlinClassMetadata? {
  return KotlinClassMetadata.read(this)
}

internal fun KmVariance.asKModifier(): KModifier? {
  return when (this) {
    KmVariance.IN -> KModifier.IN
    KmVariance.OUT -> KModifier.OUT
    KmVariance.INVARIANT -> null
  }
}

/**
 * Resolves the TypeName of this type as it would be seen in the source code, including nullability
 * and generic type parameters.
 *
 * @param flags the [Flags] associated with this type
 * @param [getTypeParameter] a function that returns the type parameter for the given index. **Only
 *     called if [TypeNameKmTypeVisitor.visitTypeParameter] is called**
 * @param useTypeAlias indicates whether or not to use type aliases or resolve their underlying
 *     types
 */
internal class TypeNameKmTypeVisitor(flags: Flags,
                                     private val getTypeParameter: ((index: Int) -> TypeName)? = null,
                                     private val useTypeAlias: Boolean = true,
                                     private val receiver: (TypeName) -> Unit) : KmTypeVisitor() {

  private val nullable = false
      //Flag.Type.IS_NULLABLE(flags)
  private var className: String? = null
  private var typeAliasName: String? = null
  private var typeAliasType: TypeName? = null
  private var flexibleTypeUpperBound: TypeName? = null
  private var outerType: TypeName? = null
  private var typeParameter: TypeName? = null
  private val argumentList = mutableListOf<TypeName>()

  override fun visitAbbreviatedType(flags: Flags): KmTypeVisitor? {
    if (!useTypeAlias) {
      return null
    }
    return TypeNameKmTypeVisitor(flags, getTypeParameter, useTypeAlias) {
      typeAliasType = it
    }
  }

  override fun visitArgument(flags: Flags, variance: KmVariance): KmTypeVisitor? {
    return TypeNameKmTypeVisitor(flags, getTypeParameter, useTypeAlias) {
      argumentList.add(
          when (variance) {
            KmVariance.IN -> WildcardTypeName.consumerOf(it)
            KmVariance.OUT -> WildcardTypeName.producerOf(it)
            KmVariance.INVARIANT -> it
          }
      )
    }
  }

  override fun visitClass(name: kotlinx.metadata.ClassName) {
    className = name
  }

  override fun visitFlexibleTypeUpperBound(flags: Flags,
                                           typeFlexibilityId: String?): KmTypeVisitor? {
    return TypeNameKmTypeVisitor(flags, getTypeParameter, useTypeAlias) {
      flexibleTypeUpperBound = WildcardTypeName.producerOf(it)
    }
  }

  override fun visitOuterType(flags: Flags): KmTypeVisitor? {
    return TypeNameKmTypeVisitor(flags, getTypeParameter, useTypeAlias) {
      outerType = WildcardTypeName.consumerOf(it)
    }
  }

  override fun visitStarProjection() {
    argumentList.add(WildcardTypeName.producerOf(ANY))
  }

  override fun visitTypeAlias(name: kotlinx.metadata.ClassName) {
    if (!useTypeAlias) {
      return
    }
    typeAliasName = name
  }

  override fun visitTypeParameter(id: Int) {
    typeParameter = getTypeParameter?.invoke(id) ?: throw IllegalStateException(
        "Visiting TypeParameter when there are no type parameters!")
  }

  override fun visitEnd() {
    var finalType = flexibleTypeUpperBound ?: outerType ?: typeParameter
    if (finalType == null) {
      if (useTypeAlias) {
        finalType = typeAliasName?.let { ClassName.bestGuess(it.replace("/", ".")) }
      }
      if (finalType == null) {
        finalType = typeAliasType ?: className?.let { ClassName.bestGuess(it.replace("/", ".")) }
            ?: throw IllegalStateException("No valid typename found!")
      }
    }

    finalType = finalType.asNullableIf(nullable)
    receiver(finalType)
  }
}

internal fun KotlinClassMetadata.Class.readClassData(): ClassData {
  lateinit var className: String
  val typeParameters = LinkedHashMap<Int, TypeVariableName>()
  val typeParamResolver = { id: Int -> typeParameters[id]!! }
  val properties = mutableListOf<PropertyData>()
  accept(object : KmClassVisitor() {
    override fun visit(flags: Flags, name: kotlinx.metadata.ClassName) {
      super.visit(flags, name)
      className = name
    }

    override fun visitProperty(flags: Flags,
                               name: String,
                               getterFlags: Flags,
                               setterFlags: Flags): KmPropertyVisitor? {
      return object : KmPropertyVisitor() {
        lateinit var type: TypeName
        override fun visitEnd() {
          properties += PropertyData(flags, name, type)
        }

        override fun visitReturnType(flags: Flags): KmTypeVisitor? {
          return TypeNameKmTypeVisitor(flags, typeParamResolver) {
            type = it
          }
        }
      }
    }
  })

  return ClassData(className.replace("/", "."),
      properties)
}

internal data class ClassData(
    val name: String,
    val properties: List<PropertyData>
) {
  fun getPropertyOrNull(methodElement: ExecutableElement): PropertyData? {
    return methodElement.simpleName.toString()
        .takeIf { it.endsWith(kotlinPropertyAnnotationsFunPostfix) }
        ?.substringBefore(kotlinPropertyAnnotationsFunPostfix)
        ?.let { propertyName -> properties.firstOrNull { propertyName == it.name } }
  }

  val className: String = name.takeLastWhile { it != '.' }

  companion object {
    /**
     * Postfix of the method name containing the [kotlin.Metadata] annotation for the relative property.
     * @see [getPropertyOrNull]
     */
    const val kotlinPropertyAnnotationsFunPostfix = "\$annotations"
  }
}

internal data class ConstructorData(
    val flags: Flags,
    val parameters: List<ParameterData>
)

internal data class ParameterData(
    val flags: Flags,
    val name: String,
    val type: TypeName,
    val isVarArg: Boolean = false
)

internal data class PropertyData(
    val flags: Flags,
    val name: String,
    val type: TypeName
)
