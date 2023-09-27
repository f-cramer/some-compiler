package de.cramer.compiler.syntax.expression

import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.Token

data class ParenthesizedExpression(
    val openParenthesisToken: Token,
    val expression: ExpressionNode,
    val closeParenthesisToken: Token,
) : ExpressionNode {
    override val type = SyntaxType.ParenthesizedExpression
}
