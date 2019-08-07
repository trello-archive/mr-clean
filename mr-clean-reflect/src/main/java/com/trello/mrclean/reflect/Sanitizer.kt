package com.trello.mrclean.reflect

import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

inline fun <reified T: Any> T.reflectedToString(): String {
  val instance = this
  val sanitizedString = instance::class.memberProperties
      .joinToString {
        "${it.name} = ${it.javaGetter?.invoke(instance)}"
      }
  return "${instance::class.simpleName}( $sanitizedString )"
}

