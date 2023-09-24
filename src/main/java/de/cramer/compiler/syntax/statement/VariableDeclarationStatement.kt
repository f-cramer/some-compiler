package de.cramer.compiler.syntax.statement

import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.Token
import de.cramer.compiler.syntax.expression.ExpressionNode

data class VariableDeclarationStatement(
    val keyword: Token,
    val identifier: Token,
    val equalsToken: Token,
    val initializer: ExpressionNode,
) : StatementNode {
    override val type = SyntaxType.VariableDeclarationStatement
    override val children = listOf(keyword, identifier, equalsToken, initializer)
}
