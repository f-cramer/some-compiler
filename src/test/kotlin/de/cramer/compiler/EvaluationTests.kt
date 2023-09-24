package de.cramer.compiler

import assertk.Assert
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import assertk.assertions.support.appendName
import de.cramer.compiler.binding.VariableSymbol
import de.cramer.compiler.syntax.SyntaxTree
import de.cramer.compiler.text.SourceText
import de.cramer.compiler.utils.AnnotatedText
import org.junit.jupiter.api.Test
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

    @Test
    fun `variable declaration reports redeclaration`() {
        val text = """
            {
                var x = 10
                var y = 100
                {
                    var x = 10
                }
                var [x] = 5
            }
        """

        val diagnostics = """
            variable 'x' has already been declared
        """

        assertThat(text).hasDiagnostics(diagnostics)
    }

    @Test
    fun `name reports undefined`() {
        val text = "[x] * 10"

        val diagnostics = """
            variable 'x' is not defined
        """

        assertThat(text).hasDiagnostics(diagnostics)
    }

    @Test
    fun `assignment reports reassignment`() {
        val text = """
            {
                val x = 5
                x [=] 10
            }
        """

        val diagnostics = """
            variable 'x' is read-only and cannot be reassigned
        """

        assertThat(text).hasDiagnostics(diagnostics)
    }

    @Test
    fun `assignment reports invalid assignment`() {
        val text = """
            {
                var x = 5
                x = [true]
            }
        """

        val diagnostics = """
            cannot assign expression of type 'boolean' to variable of type 'int'
        """

        assertThat(text).hasDiagnostics(diagnostics)
    }

    @Test
    fun `unary reports undefined operator`() {
        val text = "[+]true"

        val diagnostics = """
            unary operator '+' is not defined for type 'boolean'
        """

        assertThat(text).hasDiagnostics(diagnostics)
    }

    @Test
    fun `binary reports undefined operator`() {
        val text = "1 [&&] 2"

        val diagnostics = """
            binary operator '&&' is not defined for types 'int' and 'int'
        """

        assertThat(text).hasDiagnostics(diagnostics)
    }

    private fun Assert<String>.hasDiagnostics(diagnosticTexts: String) {
        hasDiagnostics(diagnosticTexts.trimIndent().lines())
    }

    private fun Assert<String>.hasDiagnostics(diagnosticTexts: List<String>) {
        given {
            val annotatedText = AnnotatedText(it.trimIndent())
            if (annotatedText.spans.size != diagnosticTexts.size) {
                error("diagnosticTexts.size does not match number of spans in code")
            }
            val diagnostics = annotatedText.spans.zip(diagnosticTexts)
                .map { (span, message) -> Diagnostic(span, message) }

            transform(appendName("diagnostics", separator = ".")) {
                val syntaxTree = SyntaxTree.parse(SourceText(annotatedText.text))
                val compilation = Compilation(syntaxTree)
                val result = compilation.evaluate(mutableMapOf())
                assertThat(result, name = "evaluation result").isInstanceOf<EvaluationResult.Failure>()
                result as EvaluationResult.Failure
                result.diagnostics
            }.containsExactlyInAnyOrder(*diagnostics.toTypedArray())
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
            Arguments.of("3 < 4", true),
            Arguments.of("5 < 4", false),
            Arguments.of("4 <= 4", true),
            Arguments.of("5 <= 4", false),
            Arguments.of("4 > 3", true),
            Arguments.of("4 > 5", false),
            Arguments.of("4 >= 4", true),
            Arguments.of("4 >= 5", false),
            Arguments.of("false == false", true),
            Arguments.of("true == false", false),
            Arguments.of("false != false", false),
            Arguments.of("true != false", true),
            Arguments.of("true", true),
            Arguments.of("false", false),
            Arguments.of("!true", false),
            Arguments.of("!false", true),
            Arguments.of("{ var a = 0 (a = 10) * a }", 100),
        )
    }
}
