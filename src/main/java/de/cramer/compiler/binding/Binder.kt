package de.cramer.compiler.binding

import de.cramer.compiler.Diagnostic
import de.cramer.compiler.Diagnostics
import de.cramer.compiler.syntax.AssignmentExpression
import de.cramer.compiler.syntax.BinaryExpression
import de.cramer.compiler.syntax.ExpressionNode
import de.cramer.compiler.syntax.LiteralExpression
import de.cramer.compiler.syntax.NameExpression
import de.cramer.compiler.syntax.ParenthesizedExpression
import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.Token
import de.cramer.compiler.syntax.UnaryExpression

class Binder(
    private val variables: MutableMap<VariableSymbol, Any>,
) {
    private val diagnostics = Diagnostics()

    fun diagnostics(): List<Diagnostic> = diagnostics

    fun bindExpression(expression: ExpressionNode): BoundExpression {
        return when (expression) {
            is ParenthesizedExpression -> bindParameterizedExpression(expression)
            is LiteralExpression -> bindLiteralExpression(expression)
            is UnaryExpression -> bindUnaryExpression(expression)
            is BinaryExpression -> bindBinaryExpression(expression)
            is NameExpression -> bindNameExpression(expression)
            is AssignmentExpression -> bindAssignmentExpression(expression)
        }
    }

    private fun bindParameterizedExpression(expression: ParenthesizedExpression) =
        bindExpression(expression.expression)

    private fun bindLiteralExpression(expression: LiteralExpression): BoundExpression {
        val value = expression.literalToken.value ?: 0
        val type = when (expression.literalToken.type) {
            SyntaxType.StringToken -> builtInTypeString
            SyntaxType.NumberToken -> builtInTypeInt
            SyntaxType.TrueKeyword, SyntaxType.FalseKeyword -> builtInTypeBoolean
            else -> error("unable to get type for ${expression.literalToken.type}")
        }
        return BoundLiteralExpression(value, type)
    }

    private fun bindUnaryExpression(expression: UnaryExpression): BoundExpression {
        val boundOperand = bindExpression(expression.operand)
        val boundOperator = bindUnaryOperator(expression.operator.type, boundOperand.type)
        if (boundOperator == null) {
            diagnostics.unknownUnaryOperator(expression.operator, boundOperand.type)
            return boundOperand
        }
        return BoundUnaryExpression(boundOperator, boundOperand)
    }

    private fun bindUnaryOperator(type: SyntaxType, operandType: Type): BoundUnaryOperator? =
        findBuiltInUnaryOperator(type, operandType)

    private fun bindBinaryExpression(expression: BinaryExpression): BoundExpression {
        val boundLeftExpression = bindExpression(expression.left)
        val boundRightExpression = bindExpression(expression.right)
        val boundOperator = bindBinaryOperator(expression.operator.type, boundLeftExpression.type, boundRightExpression.type)
        if (boundOperator == null) {
            diagnostics.unknownBinaryOperator(expression.operator, boundLeftExpression.type, boundRightExpression.type)
            return boundLeftExpression
        }
        return BoundBinaryExpression(boundLeftExpression, boundOperator, boundRightExpression)
    }

    private fun bindBinaryOperator(type: SyntaxType, leftType: Type, rightType: Type): BoundBinaryOperator? =
        findBuiltInBinaryOperator(type, leftType, rightType)

    private fun bindNameExpression(expression: NameExpression): BoundExpression {
        val name = expression.identifier.text
        val variable = variables.keys.find { it.name == name }
        if (variable == null) {
            diagnostics.undefinedName(expression.identifier, name)
            return BoundLiteralExpression(0, builtInTypeInt)
        }

        return BoundVariableExpression(variable)
    }

    private fun bindAssignmentExpression(expression: AssignmentExpression): BoundExpression {
        val name = expression.identifier.text
        val boundExpression = bindExpression(expression.value)

        val existingVariable = variables.keys.find { it.name == name }
        if (existingVariable != null) {
            if (existingVariable.type != boundExpression.type) {
                diagnostics.incompatibleAssignment(expression.equalsToken, existingVariable.type, boundExpression.type)
            }
        }

        val variable = VariableSymbol(name, boundExpression.type)
        variables[variable] = Unit
        return BoundAssignmentExpression(variable, boundExpression)
    }
}

fun Diagnostics.unknownUnaryOperator(operator: Token, type: Type) {
    this += Diagnostic(operator.span, "unary operator '${operator.text}' is not defined for type ${type.name}")
}

fun Diagnostics.unknownBinaryOperator(operator: Token, leftType: Type, rightType: Type) {
    this += Diagnostic(operator.span, "binary operator '${operator.text}' is not defined for types ${leftType.name} and ${rightType.name}")
}

fun Diagnostics.undefinedName(identifier: Token, name: String) {
    this += Diagnostic(identifier.span, "variable '$name' is not defined")
}

private fun Diagnostics.incompatibleAssignment(equalsToken: Token, variableType: Type, expressionType: Type) {
    this += Diagnostic(equalsToken.span, "cannot assign expression of type ${expressionType.name} to variable of type ${variableType.name}")
}
