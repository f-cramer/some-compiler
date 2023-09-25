package de.cramer.compiler.syntax.expression

import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.Token

data class NameExpression(
    val identifier: Token,
) : ExpressionNode {
    override val type = SyntaxType.NameExpression
    override val children = listOf(identifier)
}
