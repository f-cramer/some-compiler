package de.cramer.compiler.syntax.statement

import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.Token

data class BlockStatement(
    val openBraceToken: Token,
    val statements: List<StatementNode>,
    val closeBraceToken: Token,
) : StatementNode {
    override val type = SyntaxType.BlockStatement
    override val children = listOf(
        listOf(openBraceToken),
        statements,
        listOf(closeBraceToken),
    ).flatten()
}
