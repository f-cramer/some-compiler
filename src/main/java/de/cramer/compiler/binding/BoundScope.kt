package de.cramer.compiler.binding

import de.cramer.compiler.syntax.CodePointString

class BoundScope(
    private val parent: BoundScope?,
) {
    private val variables = mutableMapOf<CodePointString, VariableSymbol>()

    val declaredVariables: Collection<VariableSymbol>
        get() = variables.values

    fun declare(variable: VariableSymbol): Boolean =
        if (variables.containsKey(variable.name)) {
            false
        } else {
            variables[variable.name] = variable
            true
        }

    operator fun get(name: CodePointString): VariableSymbol? =
        if (variables.containsKey(name)) {
            variables[name]
        } else {
            parent?.get(name)
        }
}
