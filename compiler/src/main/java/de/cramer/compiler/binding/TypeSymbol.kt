package de.cramer.compiler.binding

val anyTypeMatcher: TypeMatcher = { true }

private val builtInTypeMatchers = mutableMapOf<TypeSymbol, TypeMatcher>()

data class TypeSymbol(
    val name: String,
    val builtin: Boolean = false,
) {
    override fun toString(): String = name

    companion object {
        val int = TypeSymbol("int", true)
        val boolean = TypeSymbol("boolean", true)
        val string = TypeSymbol("string", true)
    }
}

typealias TypeMatcher = (TypeSymbol) -> Boolean

fun TypeMatcher(type: TypeSymbol): TypeMatcher = builtInTypeMatchers.computeIfAbsent(type) { { it == type } }
