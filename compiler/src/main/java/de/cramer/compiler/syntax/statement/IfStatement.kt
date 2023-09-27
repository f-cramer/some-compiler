package de.cramer.compiler.syntax.statement

import de.cramer.compiler.syntax.SyntaxNode
import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.Token
import de.cramer.compiler.syntax.expression.ExpressionNode

data class IfStatement(
    val keyword: Token,
    val condition: ExpressionNode,
    val thenStatement: StatementNode,
    val elseClause: ElseClause?,
) : StatementNode {
    override val type = SyntaxType.IfStatement
}

data class ElseClause(
    val keyword: Token,
    val statement: StatementNode,
) : SyntaxNode {
    override val type = SyntaxType.ElseClause
}
