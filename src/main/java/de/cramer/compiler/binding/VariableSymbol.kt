package de.cramer.compiler.binding

import de.cramer.compiler.syntax.CodePointString

data class VariableSymbol(
    val name: CodePointString,
    val type: Type,
)
