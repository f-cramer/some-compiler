package de.cramer.compiler.syntax.expression

import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.Token

data class LiteralExpression(
    val literalToken: Token,
) : ExpressionNode {
    override val type = SyntaxType.LiteralExpression
}
