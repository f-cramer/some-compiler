package de.cramer.compiler.binding

val builtInTypeInt = TypeSymbol("int", true)
val builtInTypeBoolean = TypeSymbol("boolean", true)
val builtInTypeString = TypeSymbol("string", true)

val anyTypeMatcher: TypeMatcher = { true }

private val builtInTypeMatchers = mutableMapOf<TypeSymbol, TypeMatcher>()

data class TypeSymbol(
    val name: String,
    val builtin: Boolean = false,
) {
    override fun toString(): String = name
}

typealias TypeMatcher = (TypeSymbol) -> Boolean

fun TypeMatcher(type: TypeSymbol): TypeMatcher = builtInTypeMatchers.computeIfAbsent(type) { { it == type } }
