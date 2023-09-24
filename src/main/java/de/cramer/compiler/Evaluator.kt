package de.cramer.compiler

import de.cramer.compiler.binding.BoundAssignmentExpression
import de.cramer.compiler.binding.BoundBinaryExpression
import de.cramer.compiler.binding.BoundBlockStatement
import de.cramer.compiler.binding.BoundExpression
import de.cramer.compiler.binding.BoundExpressionStatement
import de.cramer.compiler.binding.BoundIfStatement
import de.cramer.compiler.binding.BoundLiteralExpression
import de.cramer.compiler.binding.BoundStatement
import de.cramer.compiler.binding.BoundUnaryExpression
import de.cramer.compiler.binding.BoundVariableDeclarationStatement
import de.cramer.compiler.binding.BoundVariableExpression
import de.cramer.compiler.binding.VariableSymbol
import de.cramer.compiler.binding.binaryOperatorAdditionIntInt
import de.cramer.compiler.binding.binaryOperatorAdditionStringString
import de.cramer.compiler.binding.binaryOperatorDivisionIntInt
import de.cramer.compiler.binding.binaryOperatorEqualsAny
import de.cramer.compiler.binding.binaryOperatorGreaterIntInt
import de.cramer.compiler.binding.binaryOperatorGreaterOrEqualIntInt
import de.cramer.compiler.binding.binaryOperatorLessIntInt
import de.cramer.compiler.binding.binaryOperatorLessOrEqualIntInt
import de.cramer.compiler.binding.binaryOperatorLogicalAndBooleanBoolean
import de.cramer.compiler.binding.binaryOperatorLogicalOrBooleanBoolean
import de.cramer.compiler.binding.binaryOperatorLogicalXorBooleanBoolean
import de.cramer.compiler.binding.binaryOperatorMultiplicationIntInt
import de.cramer.compiler.binding.binaryOperatorNotEqualsAny
import de.cramer.compiler.binding.binaryOperatorSubtractionIntInt
import de.cramer.compiler.binding.unaryOperatorIdentityInt
import de.cramer.compiler.binding.unaryOperatorLogicalNegationBoolean
import de.cramer.compiler.binding.unaryOperatorNegationInt

class Evaluator(
    private val root: BoundStatement,
    private val variables: MutableMap<VariableSymbol, Any>,
) {
    private var lastValue: Any? = null

    fun evaluate(): Any? {
        evaluateStatement(root)
        return lastValue
    }

    private fun evaluateStatement(statement: BoundStatement) {
        when (statement) {
            is BoundBlockStatement -> evaluateBlockStatement(statement)
            is BoundExpressionStatement -> evaluateExpressionStatement(statement)
            is BoundVariableDeclarationStatement -> evaluateVariableDeclarationStatement(statement)
            is BoundIfStatement -> evaluateIfStatement(statement)
        }
    }

    private fun evaluateBlockStatement(statement: BoundBlockStatement) {
        for (s in statement.statements) {
            evaluateStatement(s)
        }
    }

    private fun evaluateExpressionStatement(statement: BoundExpressionStatement) {
        lastValue = evaluateExpression(statement.expression)
    }

    private fun evaluateVariableDeclarationStatement(statement: BoundVariableDeclarationStatement) {
        val value = evaluateExpression(statement.initializer)
        variables[statement.variable] = value
        lastValue = value
    }

    private fun evaluateIfStatement(statement: BoundIfStatement) {
        val condition = evaluateExpression(statement.condition) as Boolean
        if (condition) {
            evaluateStatement(statement.thenStatement)
        } else if (statement.elseStatement != null) {
            evaluateStatement(statement.elseStatement)
        }
    }

    private fun evaluateExpression(expression: BoundExpression): Any {
        return when (expression) {
            is BoundLiteralExpression -> evaluateLiteralExpression(expression)
            is BoundUnaryExpression -> evaluateUnaryExpression(expression)
            is BoundBinaryExpression -> evaluateBinaryExpression(expression)
            is BoundVariableExpression -> evaluateVariableExpression(expression)
            is BoundAssignmentExpression -> evaluateAssignmentExpression(expression)
        }
    }

    private fun evaluateLiteralExpression(expression: BoundLiteralExpression): Any = expression.value

    private fun evaluateUnaryExpression(expression: BoundUnaryExpression): Any {
        val operand = evaluateExpression(expression.operand)
        return when (expression.operator) {
            unaryOperatorIdentityInt -> operand
            unaryOperatorNegationInt -> -(operand as Int)
            unaryOperatorLogicalNegationBoolean -> !(operand as Boolean)
            else -> throw NotImplementedError("evaluation for operator ${expression.operator}")
        }
    }

    private fun evaluateBinaryExpression(expression: BoundBinaryExpression): Any {
        val left = evaluateExpression(expression.left)
        val right = evaluateExpression(expression.right)

        return when (expression.operator) {
            binaryOperatorAdditionIntInt -> left as Int + right as Int
            binaryOperatorSubtractionIntInt -> left as Int - right as Int
            binaryOperatorMultiplicationIntInt -> left as Int * right as Int
            binaryOperatorDivisionIntInt -> left as Int / right as Int
            binaryOperatorLogicalAndBooleanBoolean -> left as Boolean && right as Boolean
            binaryOperatorLogicalOrBooleanBoolean -> left as Boolean || right as Boolean
            binaryOperatorLogicalXorBooleanBoolean -> left as Boolean xor right as Boolean
            binaryOperatorEqualsAny -> left == right
            binaryOperatorNotEqualsAny -> left != right
            binaryOperatorAdditionStringString -> left as String + right as String
            binaryOperatorLessIntInt -> (left as Int) < (right as Int)
            binaryOperatorLessOrEqualIntInt -> left as Int <= right as Int
            binaryOperatorGreaterIntInt -> left as Int > right as Int
            binaryOperatorGreaterOrEqualIntInt -> left as Int >= right as Int
            else -> throw NotImplementedError("evaluation for operator ${expression.operator}")
        }
    }

    private fun evaluateVariableExpression(expression: BoundVariableExpression): Any =
        variables.getValue(expression.variable)

    private fun evaluateAssignmentExpression(expression: BoundAssignmentExpression): Any {
        val value = evaluateExpression(expression.expression)
        variables[expression.variable] = value
        return value
    }
}
