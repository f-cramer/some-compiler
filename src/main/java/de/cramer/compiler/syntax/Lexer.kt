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

    fun diagnostics(): List<Diagnostic> = diagnostics

    fun lex(): List<Token> = buildList {
        while (index < text.length) {
            when (val char = peek()!!) {
                '"' -> parseString(this)
                ' ', '\n', '\r', '\t' -> parseWhitespace(this)
                else -> when {
                    char.isDigit() -> parseNumber(this)
                    char.isWhitespace() -> parseWhitespace(this)
                    char.isIdentifierStart() -> parseIdentifier(this)
                    else -> parseOperator(this)
                }
            }
        }
        this += Token(SyntaxType.EndOfFileToken, TextSpan(index, 0), "\u0000", null)
    }

    private fun peek(): Char? = if (index < text.length) text[index] else null

    private fun next(): Char? = peek()?.also { index++ }

    private fun expectNext(char: Char) {
        val position = index
        val next = next()
        if (next != char) {
            diagnostics.unexpectedCharacter(char.toString(), next, position)
        }
    }

    private fun expectNext(expectedDescription: String, predicate: (Char) -> Boolean): Char {
        val next = next()
        if (next == null || !predicate(next)) {
            diagnostics.unexpectedCharacter(expectedDescription, next, index)
        }
        return next ?: '\u0000'
    }

    private fun parseString(tokens: MutableList<Token>) {
        val position = index
        val value = buildString {
            expectNext('"')

            var escaped = false
            while (true) {
                val next = next()
                if (next == null) {
                    diagnostics.unexpectedCharacter("\"", null, index - 1)
                    break
                }

                if (escaped) {
                    append(next)
                } else {
                    when (next) {
                        '\\' -> escaped = true
                        '"' -> break
                        else -> append(next)
                    }
                }
            }
        }
        tokens += Token(SyntaxType.StringToken, TextSpan(position, value.length + 2), "\"$value\"", value)
    }

    private fun parseWhitespace(tokens: MutableList<Token>) {
        val position = index
        val value = buildString {
            append(expectNext("whitespace") { it.isWhitespace() })

            while (peek()?.isWhitespace() == true) {
                val next = next()!!
                append(next)
            }
        }
        tokens += Token(SyntaxType.WhitespaceToken, TextSpan(position, value.length), value, null)
    }

    private fun parseNumber(tokens: MutableList<Token>) {
        val position = index
        val value = buildString {
            append(expectNext("digit") { it.isDigit() })

            while (peek()?.isDigit() == true) {
                val next = next()!!
                append(next)
            }
        }
        val numberValue = runCatching { value.toInt() }.getOrElse {
            diagnostics.invalidInt(value, TextSpan(position, value.length))
            0
        }
        tokens += Token(SyntaxType.NumberToken, TextSpan(position, value.length), value, numberValue)
    }

    private fun parseIdentifier(tokens: MutableList<Token>) {
        val position = index
        val value = buildString {
            append(expectNext("identifier start") { it.isIdentifierStart() })

            while (peek()?.isIdentifierPart() == true) {
                val next = next()!!
                append(next)
            }
        }
        val (type, tokenValue: Any?) = when (value) {
            "true" -> SyntaxType.TrueKeyword to true
            "false" -> SyntaxType.FalseKeyword to false
            else -> SyntaxType.IdentifierToken to value
        }
        tokens += Token(type, TextSpan(position, index - position), value, tokenValue)
    }

    private fun parseOperator(tokens: MutableList<Token>) {
        val position = index
        val token = when (val next = next()) {
            '+' -> SyntaxType.PlusToken
            '-' -> SyntaxType.MinusToken
            '*' -> SyntaxType.AsteriskToken
            '/' -> SyntaxType.SlashToken
            '(' -> SyntaxType.OpenParenthesisToken
            ')' -> SyntaxType.CloseParenthesisToken
            '{' -> SyntaxType.OpenBracketToken
            '}' -> SyntaxType.CloseBracketToken
            '^' -> SyntaxType.CircumflexToken
            '&' -> if (peek() == '&') {
                next()
                SyntaxType.AmpersandAmpersandToken
            } else {
                SyntaxType.AmpersandToken
            }

            '|' -> if (peek() == '|') {
                next()
                SyntaxType.PipePipeToken
            } else {
                SyntaxType.PipeToken
            }

            '=' -> if (peek() == '=') {
                next()
                SyntaxType.EqualsEqualsToken
            } else {
                SyntaxType.EqualsToken
            }

            '!' -> if (peek() == '=') {
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

    private fun Diagnostics.unexpectedCharacter(expected: String, actual: Char?, position: Int) {
        val message = "unexpected character: expected $expected but got ${actual ?: "eof"}"
        this += Diagnostic(TextSpan(position, 1), message)
    }

    private fun Diagnostics.invalidInt(value: String, span: TextSpan) {
        val message = "$value is not a valid int"
        this += Diagnostic(span, message)
    }
}
