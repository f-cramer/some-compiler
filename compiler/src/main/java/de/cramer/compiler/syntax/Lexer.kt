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
                if (next == null || next.singleChar { it == '\n' || it == '\r' }) {
                    val end = if (next == null) index else index - 1
                    diagnostics.unterminatedString(TextSpan(position..<end))
                    break
                }

                if (escaped) {
                    val escapedChar = next.singleOrNull()?.getEscapedChar()
                    if (escapedChar == null) {
                        diagnostics.invalidEscapeSequence(next, TextSpan(index - 2, 2))
                    } else {
                        this += escapedChar.asCodePoint()
                    }
                    escaped = false
                } else {
                    when (next.singleOrNull()) {
                        '\\' -> escaped = true
                        '"' -> break
                        else -> this += next
                    }
                }
            }
        }.asCodePoints()

        val span = TextSpan(position..<index)
        tokens += Token(SyntaxType.StringToken, span, text.substring(span), value.toString())
    }

    private fun Char.getEscapedChar() = when (this) {
        '"' -> '"'
        '\\' -> '\\'
        't' -> '\t'
        'n' -> '\n'
        'r' -> '\r'
        else -> null
    }

    private fun parseWhitespace(tokens: MutableList<Token>) {
        val position = index

        expectNext("whitespace") { it.isWhitespace() }
        while (peek()?.isWhitespace() == true) {
            next()
        }

        val value = text.substring(position, index)
        tokens += Token(SyntaxType.WhitespaceToken, TextSpan(position..<index), value, null)
    }

    private fun parseNumber(tokens: MutableList<Token>) {
        val position = index

        expectNext("digit") { it.isDigit() }
        while (peek()?.isDigit() == true) {
            next()
        }

        val value = text.substring(position, index)
        val numberValue = runCatching { value.toString().toInt() }.getOrElse {
            diagnostics.invalidInt(value, TextSpan(position..<index))
            0
        }
        tokens += Token(SyntaxType.NumberToken, TextSpan(position..<index), value, numberValue)
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
            "var" -> SyntaxType.VarKeyword to null
            "val" -> SyntaxType.ValKeyword to null
            "if" -> SyntaxType.IfKeyword to null
            "else" -> SyntaxType.ElseKeyword to null
            "while" -> SyntaxType.WhileKeyword to null
            "for" -> SyntaxType.ForKeyword to null
            "to" -> SyntaxType.ToKeyword to null
            else -> SyntaxType.IdentifierToken to value
        }
        tokens += Token(type, TextSpan(position..<index), value, tokenValue)
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
            '~' -> SyntaxType.TildeToken
            '<' -> if (peek().isEqualTo('=')) {
                next()
                SyntaxType.LessOrEqualToken
            } else {
                SyntaxType.LessToken
            }

            '>' -> if (peek().isEqualTo('=')) {
                next()
                SyntaxType.GreaterOrEqualToken
            } else {
                SyntaxType.GreaterToken
            }

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

    private fun Diagnostics.unterminatedString(span: TextSpan) {
        val message = "unterminated string literal"
        this += Diagnostic(span, message)
    }

    private fun Diagnostics.invalidEscapeSequence(codePoint: CodePoint, span: TextSpan) {
        val message = "invalid escape sequence '\\$codePoint'"
        this += Diagnostic(span, message)
    }
}
