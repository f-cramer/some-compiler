package de.cramer.compiler.syntax.expression

import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.Token

data class UnaryExpression(
    val operator: Token,
    val operand: ExpressionNode,
) : ExpressionNode {
    override val type = SyntaxType.UnaryExpression
    override val children = listOf(operator, operand)
}
