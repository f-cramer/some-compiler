package de.cramer.compiler.syntax

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import de.cramer.compiler.Compilation
import de.cramer.compiler.EvaluationResult
import de.cramer.compiler.binding.VariableSymbol
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class EvaluationTests {

    @ParameterizedTest
    @MethodSource("getEvaluationTestData")
    fun `evaluation produces correct results`(text: String, expectedValue: Any) {
        val syntaxTree = SyntaxTree.parse(text)
        val compilation = Compilation(syntaxTree)
        val variables = mutableMapOf<VariableSymbol, Any>()
        val result = compilation.evaluate(variables)
        val resultAssert = assertThat(result)

        when (result) {
            is EvaluationResult.Success -> resultAssert.isInstanceOf<EvaluationResult.Success>()
                .prop(EvaluationResult.Success::value).isEqualTo(expectedValue)

            is EvaluationResult.Failure -> resultAssert.isInstanceOf<EvaluationResult.Failure>()
                .prop(EvaluationResult.Failure::diagnostics).isEmpty()
        }
    }

    companion object {
        @JvmStatic
        fun getEvaluationTestData() = listOf(
            Arguments.of("1", 1),
            Arguments.of("+1", 1),
            Arguments.of("-1", -1),
            Arguments.of("14 + 12", 26),
            Arguments.of("12 - 3", 9),
            Arguments.of("4 * 2", 8),
            Arguments.of("9 / 3", 3),
            Arguments.of("(10)", 10),
            Arguments.of("12 == 3", false),
            Arguments.of("3 == 3", true),
            Arguments.of("12 != 3", true),
            Arguments.of("3 != 3", false),
            Arguments.of("false == false", true),
            Arguments.of("true == false", false),
            Arguments.of("false != false", false),
            Arguments.of("true != false", true),
            Arguments.of("true", true),
            Arguments.of("false", false),
            Arguments.of("!true", false),
            Arguments.of("!false", true),
            Arguments.of("(a = 10) * a", 100),
            Arguments.of("{ a = 0 (a = 10) * a }", 100),
        )
    }
}
