package de.cramer.compiler.syntax

data class ParenthesizedExpression(
    val openParenthesisToken: Token,
    val expression: ExpressionNode,
    val closeParenthesisToken: Token,
) : ExpressionNode {
    override val type = SyntaxType.ParenthesizedExpression
    override val children = listOf(openParenthesisToken, expression, closeParenthesisToken)
}
