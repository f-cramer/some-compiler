package de.cramer.compiler.syntax

import de.cramer.compiler.Diagnostic
import de.cramer.compiler.Diagnostics
import de.cramer.compiler.text.SourceText
import de.cramer.compiler.text.TextSpan

class Parser private constructor(
    private val text: SourceText,
    private val rawTokens: List<Token>,
    private val tokens: List<Token>,
) {
    private var index = 0
    private val diagnostics = Diagnostics()
    private val current: Token
        get() = peek(0)

    fun diagnostics(): List<Diagnostic> = diagnostics.toList()

    private fun peek(offset: Int): Token {
        val index = this.index + offset
        return if (index >= tokens.size) {
            tokens.last()
        } else {
            tokens[index]
        }
    }

    private fun next(): Token {
        val current = current
        index++
        return current
    }

    private fun matchToken(type: SyntaxType): Token {
        if (current.type == type) {
            return next()
        }

        diagnostics.unexpectedToken(type, current.type, current.span)
        return Token(type, current.span, emptyCodePointString, null)
    }

    fun parse(): CompilationUnit {
        val expression = parseExpression()
        val endOfFileToken = matchToken(SyntaxType.EndOfFileToken)
        return CompilationUnit(expression, endOfFileToken)
    }

    private fun parseExpression(): ExpressionNode {
        return parseAssignmentExpression()
    }

    private fun parseAssignmentExpression(): ExpressionNode {
        if (current.type == SyntaxType.IdentifierToken && peek(1).type == SyntaxType.EqualsToken) {
            val identifierToken = next()
            val equalsToken = next()
            val right = parseAssignmentExpression()
            return AssignmentExpression(identifierToken, equalsToken, right)
        }

        return parseOperatorExpression()
    }

    private fun parseOperatorExpression(parentPrecendence: Int = 0): ExpressionNode {
        var left: ExpressionNode
        val unaryOperatorPrecendence = current.type.getUnaryOperatorPrecendence()
        left = if (unaryOperatorPrecendence != null && unaryOperatorPrecendence >= parentPrecendence) {
            val operator = next()
            val operand = parseOperatorExpression(unaryOperatorPrecendence)
            UnaryExpression(operator, operand)
        } else {
            parsePrimaryExpression()
        }

        while (true) {
            val precedence = current.type.getBinaryOperatorPrecendence()
            if (precedence == null || precedence <= parentPrecendence) {
                break
            }

            val operator = next()
            val right = parseOperatorExpression(precedence)
            left = BinaryExpression(left, operator, right)
        }

        return left
    }

    private fun parsePrimaryExpression(): ExpressionNode {
        return when (current.type) {
            SyntaxType.NumberToken -> parseNumberLiteral()
            SyntaxType.TrueKeyword, SyntaxType.FalseKeyword -> parseBooleanLiteral()
            SyntaxType.StringToken -> parseStringLiteral()
            SyntaxType.IdentifierToken -> parseNameExpression()
            SyntaxType.OpenParenthesisToken -> parseParenthesizedExpression()
            else -> parseNumberLiteral()
        }
    }

    private fun parseNumberLiteral(): LiteralExpression {
        val numberToken = matchToken(SyntaxType.NumberToken)
        return LiteralExpression(numberToken)
    }

    private fun parseBooleanLiteral(): LiteralExpression {
        val isTrue = current.type == SyntaxType.TrueKeyword
        val booleanToken = matchToken(if (isTrue) SyntaxType.TrueKeyword else SyntaxType.FalseKeyword)
        return LiteralExpression(booleanToken)
    }

    private fun parseStringLiteral(): LiteralExpression {
        val stringToken = matchToken(SyntaxType.StringToken)
        return LiteralExpression(stringToken)
    }

    private fun parseNameExpression(): NameExpression {
        val identifier = matchToken(SyntaxType.IdentifierToken)
        return NameExpression(identifier)
    }

    private fun parseParenthesizedExpression(): ParenthesizedExpression {
        val open = matchToken(SyntaxType.OpenParenthesisToken)
        val expression = parseExpression()
        val close = matchToken(SyntaxType.CloseParenthesisToken)
        return ParenthesizedExpression(open, expression, close)
    }

    private fun Diagnostics.unexpectedToken(expected: SyntaxType, actual: SyntaxType, span: TextSpan) {
        val message = "unexpected token <$actual>, expected <$expected>"
        this += Diagnostic(span, message)
    }

    companion object {
        operator fun invoke(text: SourceText): Parser {
            val lexer = Lexer(text)
            val rawTokens = lexer.lex()
            val tokens = rawTokens.filterNot { it.type == SyntaxType.WhitespaceToken }
            val parser = Parser(text, rawTokens, tokens)
            parser.diagnostics += lexer.diagnostics()
            return parser
        }
    }
}
