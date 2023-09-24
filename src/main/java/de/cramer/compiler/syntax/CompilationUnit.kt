package de.cramer.compiler.syntax

import de.cramer.compiler.syntax.statement.StatementNode

data class CompilationUnit(
    val statement: StatementNode,
    val endOfFileToken: Token,
) : SyntaxNode {
    override val type = SyntaxType.CompilationUnit
    override val children = listOf(statement, endOfFileToken)
}
