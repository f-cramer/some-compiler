package de.cramer.compiler.binding

import de.cramer.compiler.Diagnostic

class BoundGlobalScope(
    val previous: BoundGlobalScope?,
    val diagnostics: List<Diagnostic>,
    val variables: Collection<VariableSymbol>,
    val expression: BoundExpression,
)
