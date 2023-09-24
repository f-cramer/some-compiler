package de.cramer.compiler.binding

sealed interface BoundStatement : BoundNode

data class BoundBlockStatement(
    val statements: List<BoundStatement>,
) : BoundStatement {
    override val kind = BoundNodeKind.BlockStatement
}

data class BoundExpressionStatement(
    val expression: BoundExpression,
) : BoundStatement {
    override val kind = BoundNodeKind.ExpressionStatement
}
