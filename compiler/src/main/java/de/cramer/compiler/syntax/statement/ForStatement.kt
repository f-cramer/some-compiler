package de.cramer.compiler.syntax.statement

import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.Token
import de.cramer.compiler.syntax.expression.ExpressionNode

data class ForStatement(
    val keyword: Token,
    val variable: Token,
    val equalsToken: Token,
    val lowerBound: ExpressionNode,
    val toToken: Token,
    val upperBound: ExpressionNode,
    val body: StatementNode,
) : StatementNode {
    override val type = SyntaxType.ForStatement
}
