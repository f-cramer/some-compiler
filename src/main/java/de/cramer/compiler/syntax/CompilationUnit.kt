package de.cramer.compiler.syntax

data class CompilationUnit(
    val expression: ExpressionNode,
    val endOfFileToken: Token,
) : SyntaxNode {
    override val type = SyntaxType.CompilationUnit
    override val children = listOf(expression, endOfFileToken)
}
