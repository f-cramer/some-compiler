package de.cramer.compiler.binding

import de.cramer.compiler.Diagnostic
import de.cramer.compiler.Diagnostics
import de.cramer.compiler.syntax.CodePointString
import de.cramer.compiler.syntax.CompilationUnit
import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.Token
import de.cramer.compiler.syntax.expression.AssignmentExpression
import de.cramer.compiler.syntax.expression.BinaryExpression
import de.cramer.compiler.syntax.expression.ExpressionNode
import de.cramer.compiler.syntax.expression.LiteralExpression
import de.cramer.compiler.syntax.expression.NameExpression
import de.cramer.compiler.syntax.expression.ParenthesizedExpression
import de.cramer.compiler.syntax.expression.UnaryExpression
import de.cramer.compiler.syntax.statement.BlockStatement
import de.cramer.compiler.syntax.statement.ExpressionStatement
import de.cramer.compiler.syntax.statement.StatementNode
import de.cramer.compiler.text.TextSpan
import java.util.ArrayDeque

class Binder(
    parent: BoundScope?,
    diagnostics: Diagnostics? = null,
) {
    private val scope: BoundScope
    private val diagnostics = diagnostics ?: Diagnostics()

    init {
        scope = BoundScope(parent)
    }

    fun diagnostics(): List<Diagnostic> = diagnostics.toList()

    fun bindStatement(statement: StatementNode): BoundStatement {
        return when (statement) {
            is BlockStatement -> bindBlockStatement(statement)
            is ExpressionStatement -> bindExpressionStatement(statement)
        }
    }

    private fun bindBlockStatement(statement: BlockStatement): BoundBlockStatement {
        val blockBinder = Binder(scope, diagnostics)
        val statements = statement.statements.map { blockBinder.bindStatement(it) }
        return BoundBlockStatement(statements)
    }

    private fun bindExpressionStatement(statement: ExpressionStatement): BoundExpressionStatement {
        val expression = bindExpression(statement.expression)
        return BoundExpressionStatement(expression)
    }

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
        val variable = scope[name]
        if (variable == null) {
            diagnostics.undefinedName(expression.identifier, name)
            return BoundLiteralExpression(0, builtInTypeInt)
        }

        return BoundVariableExpression(variable)
    }

    private fun bindAssignmentExpression(expression: AssignmentExpression): BoundExpression {
        val name = expression.identifier.text
        val boundExpression = bindExpression(expression.value)

        val variable = VariableSymbol(name, boundExpression.type)
        if (!scope.declare(variable)) {
            val existingVariable = scope[name]!!
            if (existingVariable.type != boundExpression.type) {
                diagnostics.incompatibleAssignment(expression.span, existingVariable.type, boundExpression.type)
            }
        }
        return BoundAssignmentExpression(variable, boundExpression)
    }

    companion object {
        fun bindGlobalScope(previous: BoundGlobalScope?, compilationUnit: CompilationUnit): BoundGlobalScope {
            val parentScope = createParentScopes(previous)
            val binder = Binder(parentScope)
            val statement = binder.bindStatement(compilationUnit.statement)
            val variables = binder.scope.declaredVariables
            val diagnostics = binder.diagnostics()
            return BoundGlobalScope(previous, diagnostics, variables, statement)
        }

        private fun createParentScopes(previous: BoundGlobalScope?): BoundScope? {
            var p = previous
            val stack = ArrayDeque<BoundGlobalScope>()
            while (p != null) {
                stack.push(p)
                p = p.previous
            }

            var parent: BoundScope? = null
            while (stack.isNotEmpty()) {
                val global = stack.pop()
                val scope = BoundScope(parent)
                for (variable in global.variables) {
                    scope.declare(variable)
                }

                parent = scope
            }

            return parent
        }
    }
}

fun Diagnostics.unknownUnaryOperator(operator: Token, type: Type) {
    this += Diagnostic(operator.span, "unary operator '${operator.text}' is not defined for type ${type.name}")
}

fun Diagnostics.unknownBinaryOperator(operator: Token, leftType: Type, rightType: Type) {
    this += Diagnostic(operator.span, "binary operator '${operator.text}' is not defined for types ${leftType.name} and ${rightType.name}")
}

fun Diagnostics.undefinedName(identifier: Token, name: CodePointString) {
    this += Diagnostic(identifier.span, "variable '$name' is not defined")
}

private fun Diagnostics.incompatibleAssignment(span: TextSpan, variableType: Type, expressionType: Type) {
    this += Diagnostic(span, "cannot assign expression of type ${expressionType.name} to variable of type ${variableType.name}")
}
