package de.cramer.compiler.syntax.statement

import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.expression.ExpressionNode

data class ExpressionStatement(
    val expression: ExpressionNode,
) : StatementNode {
    override val type = SyntaxType.ExpressionStatement
}
