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

data class BoundVariableDeclarationStatement(
    val variable: VariableSymbol,
    val initializer: BoundExpression,
) : BoundStatement {
    override val kind = BoundNodeKind.VariableDeclarationStatement
}

data class BoundIfStatement(
    val condition: BoundExpression,
    val thenStatement: BoundStatement,
    val elseStatement: BoundStatement?,
) : BoundStatement {
    override val kind = BoundNodeKind.IfStatement
}

data class BoundWhileStatement(
    val condition: BoundExpression,
    val body: BoundStatement,
) : BoundStatement {
    override val kind = BoundNodeKind.WhileStatement
}

data class BoundForStatement(
    val variable: VariableSymbol,
    val lowerBound: BoundExpression,
    val upperBound: BoundExpression,
    val body: BoundStatement,
) : BoundStatement {
    override val kind = BoundNodeKind.ForStatement
}
