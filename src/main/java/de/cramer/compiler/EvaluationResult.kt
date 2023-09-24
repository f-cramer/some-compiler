package de.cramer.compiler

sealed interface EvaluationResult {
    data class Success(val value: Any?) : EvaluationResult

    data class Failure(val diagnostics: List<Diagnostic>) : EvaluationResult
}
