package de.cramer.compiler.syntax

import de.cramer.compiler.Diagnostic
import de.cramer.compiler.Diagnostics
import de.cramer.compiler.text.SourceText
import de.cramer.compiler.text.TextSpan

class Lexer(
    private val text: SourceText,
) {
    private var index = 0
    private val diagnostics = Diagnostics()

    fun diagnostics(): List<Diagnostic> = diagnostics.toList()

    fun lex(): List<Token> = buildList {
        while (index < text.length) {
            val char = peek()!!
            when (char.singleOrNull()) {
                '"' -> parseString(this)
                ' ', '\n', '\r', '\t' -> parseWhitespace(this)
                else -> when {
                    char.isDigit() -> parseNumber(this)
                    char.isWhitespace() -> parseWhitespace(this)
                    char.singleChar { it.isIdentifierStart() } -> parseIdentifier(this)
                    else -> parseOperator(this)
                }
            }
        }
        this += Token(SyntaxType.EndOfFileToken, TextSpan(index, 0), "\u0000".asCodePoints(), null)
    }

    private fun peek(): CodePoint? = if (index < text.length) text[index] else null

    private fun next(): CodePoint? = peek()?.also { index++ }

    private fun expectNext(char: Char) {
        val position = index
        val next = next()
        if (next == null || !next.isEqualTo(char)) {
            diagnostics.unexpectedCharacter(char.toString(), next, position)
        }
    }

    private fun expectNext(expectedDescription: String, predicate: (Char) -> Boolean): CodePoint {
        val next = next()
        if (next == null || !next.singleChar(predicate)) {
            diagnostics.unexpectedCharacter(expectedDescription, next, index)
        }
        return next ?: CodePoint(0)
    }

    private fun parseString(tokens: MutableList<Token>) {
        val position = index

        val value = buildList {
            expectNext('"')

            var escaped = false
            while (true) {
                val next = next()
                if (next == null) {
                    diagnostics.unexpectedCharacter("\"", null, index - 1)
                    break
                }

                if (escaped) {
                    this += next
                } else {
                    when (next.singleOrNull()) {
                        '\\' -> escaped = true
                        '"' -> break
                        else -> this += next
                    }
                }
            }
        }.asCodePoints()

        tokens += Token(SyntaxType.StringToken, TextSpan(position, index - position), "\"$value\"".asCodePoints(), value.toString())
    }

    private fun parseWhitespace(tokens: MutableList<Token>) {
        val position = index

        expectNext("whitespace") { it.isWhitespace() }
        while (peek()?.isWhitespace() == true) {
            next()
        }

        val value = text.substring(position, index)
        tokens += Token(SyntaxType.WhitespaceToken, TextSpan(position, value.length), value, null)
    }

    private fun parseNumber(tokens: MutableList<Token>) {
        val position = index

        expectNext("digit") { it.isDigit() }
        while (peek()?.isDigit() == true) {
            next()
        }

        val value = text.substring(position, index)
        val numberValue = runCatching { value.toString().toInt() }.getOrElse {
            diagnostics.invalidInt(value, TextSpan(position, value.length))
            0
        }
        tokens += Token(SyntaxType.NumberToken, TextSpan(position, value.length), value, numberValue)
    }

    private fun parseIdentifier(tokens: MutableList<Token>) {
        val position = index

        expectNext("identifier start") { it.isIdentifierStart() }
        while (peek()?.singleChar { it.isIdentifierPart() } == true) {
            next()
        }

        val value = text.substring(position, index)
        val (type, tokenValue: Any?) = when (value.toString()) {
            "true" -> SyntaxType.TrueKeyword to true
            "false" -> SyntaxType.FalseKeyword to false
            else -> SyntaxType.IdentifierToken to value
        }
        tokens += Token(type, TextSpan(position, index - position), value, tokenValue)
    }

    private fun parseOperator(tokens: MutableList<Token>) {
        val position = index

        val next = next()
        val token = when (next?.singleOrNull()) {
            '+' -> SyntaxType.PlusToken
            '-' -> SyntaxType.MinusToken
            '*' -> SyntaxType.AsteriskToken
            '/' -> SyntaxType.SlashToken
            '(' -> SyntaxType.OpenParenthesisToken
            ')' -> SyntaxType.CloseParenthesisToken
            '{' -> SyntaxType.OpenBraceToken
            '}' -> SyntaxType.CloseBraceToken
            '^' -> SyntaxType.CircumflexToken
            '&' -> if (peek().isEqualTo('&')) {
                next()
                SyntaxType.AmpersandAmpersandToken
            } else {
                SyntaxType.AmpersandToken
            }

            '|' -> if (peek().isEqualTo('|')) {
                next()
                SyntaxType.PipePipeToken
            } else {
                SyntaxType.PipeToken
            }

            '=' -> if (peek().isEqualTo('=')) {
                next()
                SyntaxType.EqualsEqualsToken
            } else {
                SyntaxType.EqualsToken
            }

            '!' -> if (peek().isEqualTo('=')) {
                next()
                SyntaxType.BangEqualsToken
            } else {
                SyntaxType.BangToken
            }

            else -> {
                diagnostics.unexpectedCharacter("operator", next, position)
                SyntaxType.BadInputToken
            }
        }

        val length = index - position
        val span = TextSpan(position, length)
        tokens += Token(token, span, text.substring(span), null)
    }

    private fun Char.isIdentifierStart(): Boolean = isIdentifierPart() || isDigit()

    private fun Char.isIdentifierPart(): Boolean = isLetterOrDigit() || this == '_'

    private fun Diagnostics.unexpectedCharacter(expected: String, actual: CodePoint?, position: Int) {
        val message = "unexpected character: expected $expected but got ${actual ?: "eof"}"
        this += Diagnostic(TextSpan(position, 1), message)
    }

    private fun Diagnostics.invalidInt(value: CodePointString, span: TextSpan) {
        val message = "'$value' is not a valid int"
        this += Diagnostic(span, message)
    }
}
