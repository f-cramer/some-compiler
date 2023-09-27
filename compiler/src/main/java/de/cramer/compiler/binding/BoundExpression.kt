package de.cramer.compiler.binding

sealed interface BoundExpression : BoundNode {
    val type: TypeSymbol
    val kind: BoundExpressionKind
}

enum class BoundExpressionKind {
    LiteralExpression,
    UnaryExpression,
    BinaryExpression,
    VariableExpression,
    AssignmentExpression,
}

data class BoundLiteralExpression(
    val value: Any,
    override val type: TypeSymbol,
) : BoundExpression {
    override val kind: BoundExpressionKind
        get() = BoundExpressionKind.LiteralExpression
}

data class BoundUnaryExpression(
    val operator: BoundUnaryOperator,
    val operand: BoundExpression,
) : BoundExpression {
    override val kind: BoundExpressionKind
        get() = BoundExpressionKind.UnaryExpression
    override val type: TypeSymbol
        get() = operator.type
}

data class BoundBinaryExpression(
    val left: BoundExpression,
    val operator: BoundBinaryOperator,
    val right: BoundExpression,
) : BoundExpression {
    override val kind: BoundExpressionKind
        get() = BoundExpressionKind.BinaryExpression
    override val type: TypeSymbol
        get() = operator.type
}

data class BoundVariableExpression(
    val variable: VariableSymbol,
) : BoundExpression {
    override val kind: BoundExpressionKind
        get() = BoundExpressionKind.VariableExpression
    override val type: TypeSymbol
        get() = variable.type
}

data class BoundAssignmentExpression(
    val variable: VariableSymbol,
    val expression: BoundExpression,
) : BoundExpression {
    override val kind: BoundExpressionKind
        get() = BoundExpressionKind.AssignmentExpression
    override val type: TypeSymbol
        get() = variable.type
}
