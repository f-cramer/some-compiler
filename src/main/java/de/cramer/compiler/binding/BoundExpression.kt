package de.cramer.compiler.binding

sealed interface BoundExpression : BoundNode {
    val type: Type
}

enum class BoundNodeKind {
    Literal,
    UnaryExpression,
    BinaryExpression,
    VariableExpression,
    AssignmentExpression,
    BlockStatement,
    ExpressionStatement,
    VariableDeclarationStatement,
    IfStatement,
    WhileStatement,
    ForStatement,
}

data class BoundLiteralExpression(
    val value: Any,
    override val type: Type,
) : BoundExpression {
    override val kind = BoundNodeKind.Literal
}

data class BoundUnaryExpression(
    val operator: BoundUnaryOperator,
    val operand: BoundExpression,
) : BoundExpression {
    override val kind = BoundNodeKind.UnaryExpression
    override val type = operator.type
}

data class BoundBinaryExpression(
    val left: BoundExpression,
    val operator: BoundBinaryOperator,
    val right: BoundExpression,
) : BoundExpression {
    override val kind = BoundNodeKind.BinaryExpression
    override val type = operator.type
}

data class BoundVariableExpression(
    val variable: VariableSymbol,
) : BoundExpression {
    override val kind = BoundNodeKind.VariableExpression
    override val type = variable.type
}

data class BoundAssignmentExpression(
    val variable: VariableSymbol,
    val expression: BoundExpression,
) : BoundExpression {
    override val kind = BoundNodeKind.AssignmentExpression
    override val type = variable.type
}
