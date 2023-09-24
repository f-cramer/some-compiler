package de.cramer.compiler.syntax

sealed interface ExpressionNode : SyntaxNode

data class LiteralExpression(
    val literalToken: Token,
) : ExpressionNode {
    override val type = SyntaxType.LiteralExpression
    override val children = listOf(literalToken)
}

data class UnaryExpression(
    val operator: Token,
    val operand: ExpressionNode,
) : ExpressionNode {
    override val type = SyntaxType.UnaryExpression
    override val children = listOf(operator, operand)
}

data class BinaryExpression(
    val left: ExpressionNode,
    val operator: Token,
    val right: ExpressionNode,
) : ExpressionNode {
    override val type = SyntaxType.BinaryExpression
    override val children = listOf(left, operator, right)
}

data class NameExpression(
    val identifier: Token,
) : ExpressionNode {
    override val type = SyntaxType.NameExpression
    override val children = listOf(identifier)
}

data class AssignmentExpression(
    val identifier: Token,
    val equalsToken: Token,
    val value: ExpressionNode,
) : ExpressionNode {
    override val type = SyntaxType.AssignmentExpression
    override val children = listOf(identifier, value)
}
