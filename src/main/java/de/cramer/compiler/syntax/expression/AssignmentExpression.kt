package de.cramer.compiler.syntax.expression

import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.Token

data class AssignmentExpression(
    val identifier: Token,
    val equalsToken: Token,
    val value: ExpressionNode,
) : ExpressionNode {
    override val type = SyntaxType.AssignmentExpression
    override val children = listOf(identifier, value)
}
