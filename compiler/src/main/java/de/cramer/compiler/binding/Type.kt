package de.cramer.compiler.binding

val builtInTypeInt = Type("int", true)
val builtInTypeBoolean = Type("boolean", true)
val builtInTypeString = Type("string", true)

val anyTypeMatcher: TypeMatcher = { true }

private val builtInTypeMatchers = mutableMapOf<Type, TypeMatcher>()

data class Type(
    val name: String,
    val builtin: Boolean = false,
)

typealias TypeMatcher = (Type) -> Boolean

fun TypeMatcher(type: Type): TypeMatcher = builtInTypeMatchers.computeIfAbsent(type) { { it == type } }
