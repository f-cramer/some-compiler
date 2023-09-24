package de.cramer.compiler

import de.cramer.compiler.binding.Binder
import de.cramer.compiler.binding.BoundGlobalScope
import de.cramer.compiler.syntax.SyntaxTree

data class Compilation(
    private val previous: Compilation?,
    val syntaxTree: SyntaxTree,
) {
    constructor(syntaxTree: SyntaxTree) : this(null, syntaxTree)

    private val globalScope: BoundGlobalScope by lazy {
        Binder.bindGlobalScope(previous?.globalScope, syntaxTree.root)
    }

    fun evaluate(variables: Variables): EvaluationResult {
        val diagnostics = syntaxTree.diagnostics + globalScope.diagnostics
        if (diagnostics.isNotEmpty()) {
            return EvaluationResult.Failure(diagnostics)
        }

        val evaluator = Evaluator(globalScope.statement, variables)
        val result = evaluator.evaluate()
        return EvaluationResult.Success(result)
    }
}
