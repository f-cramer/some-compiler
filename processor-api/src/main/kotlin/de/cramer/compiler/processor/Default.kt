package de.cramer.compiler.processor

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Default(
    val value: KClass<*>,
)
