package de.cramer.compiler.syntax.statement

import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.Token
import de.cramer.compiler.syntax.expression.ExpressionNode

data class WhileStatement(
    val keyword: Token,
    val condition: ExpressionNode,
    val body: StatementNode,
) : StatementNode {
    override val type = SyntaxType.WhileStatement
    override val children = listOf(keyword, condition, body)
}
