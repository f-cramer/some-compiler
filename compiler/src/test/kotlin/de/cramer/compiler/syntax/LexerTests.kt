package de.cramer.compiler.syntax

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import assertk.assertions.single
import de.cramer.compiler.Diagnostic
import de.cramer.compiler.text.TextSpan
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class LexerTests {

    @Test
    fun `lexer tests all tokens`() {
        val types = SyntaxType.entries
            .filter { it.name.endsWith("Keyword") || it.name.endsWith("Token") }
        val testedTypes = (getTokens() + getSeparators()).map { it.type }.toSet()

        val ignoredTypes = setOf(SyntaxType.BadInputToken, SyntaxType.EndOfFileToken)
        val untestedTypes = types - testedTypes - ignoredTypes
        assertThat(untestedTypes, name = "untested tokens").isEmpty()
    }

    @MethodSource("getAllTokensData")
    @ParameterizedTest
    fun `lexer lexes tokens`(type: SyntaxType, text: String) {
        val tokens = lex(text)
        val token = assertThat(tokens).single()
        token.assert(type, text)
    }

    @MethodSource("getAllTokenPairs")
    @ParameterizedTest
    fun `lexer lexes token pairs`(type1: SyntaxType, text1: String, type2: SyntaxType, text2: String) {
        val tokens = assertThat(lex(text1 + text2))
        tokens.hasSize(2)
        tokens.index(0).assert(type1, text1)
        tokens.index(1).assert(type2, text2)
    }

    @MethodSource("getAllTokenPairsWithSeparators")
    @ParameterizedTest
    fun `lexer lexes token pairs with separators`(type1: SyntaxType, text1: String, separatorType: SyntaxType, separatorText: String, type2: SyntaxType, text2: String) {
        val tokens = assertThat(lex(text1 + separatorText + text2))
        tokens.hasSize(3)
        tokens.index(0).assert(type1, text1)
        tokens.index(1).assert(separatorType, separatorText)
        tokens.index(2).assert(type2, text2)
    }

    @Test
    fun `lexer lexes unterminated string`() {
        val text = "\"text"
        val (tokens, diagnostics) = lexWithDiagnostics(text)

        val token = assertThat(tokens).single()
        token.assert(SyntaxType.StringToken, text)

        val diagnostic = assertThat(diagnostics).single()
        diagnostic.prop(Diagnostic::message).isEqualTo("unterminated string literal")
        diagnostic.prop(Diagnostic::span).isEqualTo(TextSpan(0, 5))
    }

    @ParameterizedTest
    @MethodSource("getValidEscapeSequences")
    fun `lexer lexes valid escape sequences`(input: Char, output: Char) {
        val text = "\"\\$input\""
        val tokens = lex(text)

        val token = assertThat(tokens).single()
        token.assert(SyntaxType.StringToken, text)
        token.prop(Token::value).isEqualTo(output.toString())
    }

    @ParameterizedTest
    @MethodSource("getInvalidEscapeSequences")
    fun `lexer lexes invalid escape sequences`(input: Char) {
        val text = "\"\\$input\""
        val (tokens, diagnostics) = lexWithDiagnostics(text)

        val token = assertThat(tokens).single()
        token.assert(SyntaxType.StringToken, text)

        val diagnostic = assertThat(diagnostics).single()
        diagnostic.prop(Diagnostic::message).isEqualTo("invalid escape sequence '\\$input'")
        diagnostic.prop(Diagnostic::span).isEqualTo(TextSpan(1, 2))
    }

    companion object {
        @JvmStatic
        @Suppress("UnusedPrivateMember")
        private fun getAllTokensData() = getAllTokens().map { Arguments.of(it.type, it.text) }

        @JvmStatic
        @Suppress("UnusedPrivateMember")
        private fun getAllTokenPairs() = getTokens().flatMap { outer ->
            getTokens()
                .filterNot { inner -> requiresSeparator(outer.type, inner.type) }
                .mapNotNull { inner -> Arguments.of(outer.type, outer.text, inner.type, inner.text) }
        }

        @JvmStatic
        @Suppress("UnusedPrivateMember")
        private fun getAllTokenPairsWithSeparators() = getTokens().flatMap { outer ->
            getTokens().flatMap { inner ->
                getSeparators().map { separator ->
                    Arguments.of(outer.type, outer.text, separator.type, separator.text, inner.type, inner.text)
                }
            }
        }

        private fun getAllTokens() = getTokens() + getSeparators()

        private fun getTokens() = SyntaxType.entries
            .map { it to it.getText() }
            .filter { (_, text) -> text != null }
            .map { (type, text) -> TokenTestData(type, text!!) } +
            listOf(
                TokenTestData(SyntaxType.NumberToken, "1"),
                TokenTestData(SyntaxType.NumberToken, "123"),
                TokenTestData(SyntaxType.IdentifierToken, "a"),
                TokenTestData(SyntaxType.IdentifierToken, "abc"),
                TokenTestData(SyntaxType.StringToken, "\"a\""),
                TokenTestData(SyntaxType.StringToken, "\"abc\""),
                TokenTestData(SyntaxType.StringToken, "\"\uD83C\uDF83\""),
            )

        private fun getSeparators() = listOf(
            TokenTestData(SyntaxType.WhitespaceToken, " "),
            TokenTestData(SyntaxType.WhitespaceToken, "  "),
            TokenTestData(SyntaxType.WhitespaceToken, "\r"),
            TokenTestData(SyntaxType.WhitespaceToken, "\n"),
            TokenTestData(SyntaxType.WhitespaceToken, "\r\n"),
        )

        private fun requiresSeparator(type1: SyntaxType, type2: SyntaxType): Boolean {
            if (type1 == SyntaxType.IdentifierToken && type2 == SyntaxType.IdentifierToken) return true

            val type1IsKeyword = type1.name.endsWith("Keyword")
            val type2IsKeyword = type2.name.endsWith("Keyword")
            if (type1IsKeyword && type2IsKeyword) return true
            if (type1IsKeyword && type2 == SyntaxType.IdentifierToken) return true
            if (type1 == SyntaxType.IdentifierToken && type2IsKeyword) return true
            if (type1IsKeyword && type2 == SyntaxType.NumberToken) return true

            if (type1 == SyntaxType.NumberToken && type2 == SyntaxType.NumberToken) return true
            if (type1 == SyntaxType.IdentifierToken && type2 == SyntaxType.NumberToken) return true

            if (type1 == SyntaxType.AmpersandToken && type2 == SyntaxType.AmpersandToken) return true
            if (type1 == SyntaxType.AmpersandToken && type2 == SyntaxType.AmpersandAmpersandToken) return true
            if (type1 == SyntaxType.PipeToken && type2 == SyntaxType.PipeToken) return true
            if (type1 == SyntaxType.PipeToken && type2 == SyntaxType.PipePipeToken) return true
            if (type1 == SyntaxType.EqualsToken && type2 == SyntaxType.EqualsToken) return true
            if (type1 == SyntaxType.EqualsToken && type2 == SyntaxType.EqualsEqualsToken) return true
            if (type1 == SyntaxType.BangToken && type2 == SyntaxType.EqualsToken) return true
            if (type1 == SyntaxType.BangToken && type2 == SyntaxType.EqualsEqualsToken) return true
            if (type1 == SyntaxType.LessToken && type2 == SyntaxType.EqualsToken) return true
            if (type1 == SyntaxType.LessToken && type2 == SyntaxType.EqualsEqualsToken) return true
            if (type1 == SyntaxType.GreaterToken && type2 == SyntaxType.EqualsToken) return true
            if (type1 == SyntaxType.GreaterToken && type2 == SyntaxType.EqualsEqualsToken) return true

            return false
        }

        private data class TokenTestData(
            val type: SyntaxType,
            val text: String,
        )

        private val validEscapeCharacters = mapOf('n' to '\n', 'r' to '\r', 't' to '\t', '"' to '"', '\\' to '\\')
        private val invalidEscapeCharacters: CharArray = (0..255).asSequence()
            .map { it.toChar() }
            .filterNot { it in validEscapeCharacters }
            .filterNot { it == '\n' || it == '\r' }
            .toList()
            .toCharArray()

        @JvmStatic
        @Suppress("UnusedPrivateMember")
        private fun getValidEscapeSequences() = validEscapeCharacters.map { (input, output) ->
            Arguments.of(input, output)
        }

        @JvmStatic
        @Suppress("UnusedPrivateMember")
        private fun getInvalidEscapeSequences() = invalidEscapeCharacters.toList()
    }
}
