package de.cramer.compiler.binding

sealed interface BoundStatement : BoundNode {
    val kind: BoundStatementKind
}

enum class BoundStatementKind {
    BlockStatement,
    ExpressionStatement,
    VariableDeclarationStatement,
    IfStatement,
    WhileStatement,
    ForStatement,
    LabelStatement,
    GotoStatement,
    ConditionalGotoStatement,
}

data class BoundBlockStatement(
    val statements: List<BoundStatement>,
) : BoundStatement {
    override val kind: BoundStatementKind
        get() = BoundStatementKind.BlockStatement
}

data class BoundExpressionStatement(
    val expression: BoundExpression,
) : BoundStatement {
    override val kind: BoundStatementKind
        get() = BoundStatementKind.ExpressionStatement
}

data class BoundVariableDeclarationStatement(
    val variable: VariableSymbol,
    val initializer: BoundExpression,
) : BoundStatement {
    override val kind: BoundStatementKind
        get() = BoundStatementKind.VariableDeclarationStatement
}

data class BoundIfStatement(
    val condition: BoundExpression,
    val thenStatement: BoundStatement,
    val elseStatement: BoundStatement?,
) : BoundStatement {
    override val kind: BoundStatementKind
        get() = BoundStatementKind.IfStatement
}

data class BoundWhileStatement(
    val condition: BoundExpression,
    val body: BoundStatement,
) : BoundStatement {
    override val kind: BoundStatementKind
        get() = BoundStatementKind.WhileStatement
}

data class BoundForStatement(
    val variable: VariableSymbol,
    val lowerBound: BoundExpression,
    val upperBound: BoundExpression,
    val body: BoundStatement,
) : BoundStatement {
    override val kind: BoundStatementKind
        get() = BoundStatementKind.ForStatement
}

data class BoundLabelStatement(
    val label: LabelSymbol,
) : BoundStatement {
    override val kind: BoundStatementKind
        get() = BoundStatementKind.LabelStatement
}

data class BoundGotoStatement(
    val label: LabelSymbol,
) : BoundStatement {
    override val kind: BoundStatementKind
        get() = BoundStatementKind.GotoStatement
}

data class BoundConditionalGotoStatement(
    val label: LabelSymbol,
    val condition: BoundExpression,
    val jumpIf: Boolean,
) : BoundStatement {
    override val kind: BoundStatementKind
        get() = BoundStatementKind.ConditionalGotoStatement
}
