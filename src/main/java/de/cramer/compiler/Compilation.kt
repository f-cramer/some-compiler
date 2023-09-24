package de.cramer.compiler

import de.cramer.compiler.binding.Binder
import de.cramer.compiler.binding.VariableSymbol
import de.cramer.compiler.syntax.SyntaxTree

data class Compilation(
    val syntax: SyntaxTree,
) {
    fun evaluate(variables: MutableMap<VariableSymbol, Any>): EvaluationResult {
        val binder = Binder(variables)
        val expression = binder.bindExpression(syntax.root.expression)

        val diagnostics = syntax.diagnostics + binder.diagnostics()
        if (diagnostics.isNotEmpty()) {
            return EvaluationResult.Failure(diagnostics)
        }

        val evaluator = Evaluator(expression, variables)
        val result = evaluator.evaluate()
        return EvaluationResult.Success(result)
    }
}
