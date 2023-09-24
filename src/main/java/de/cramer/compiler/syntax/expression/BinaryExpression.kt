package de.cramer.compiler.syntax.expression

import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.Token

data class BinaryExpression(
    val left: ExpressionNode,
    val operator: Token,
    val right: ExpressionNode,
) : ExpressionNode {
    override val type = SyntaxType.BinaryExpression
    override val children = listOf(left, operator, right)
}
