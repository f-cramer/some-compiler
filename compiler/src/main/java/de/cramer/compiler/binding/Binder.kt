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
import de.cramer.compiler.syntax.statement.ForStatement
import de.cramer.compiler.syntax.statement.IfStatement
import de.cramer.compiler.syntax.statement.StatementNode
import de.cramer.compiler.syntax.statement.VariableDeclarationStatement
import de.cramer.compiler.syntax.statement.WhileStatement
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
        return when (statement.type) {
            SyntaxType.BlockStatement -> bindBlockStatement(statement as BlockStatement)
            SyntaxType.ExpressionStatement -> bindExpressionStatement(statement as ExpressionStatement)
            SyntaxType.VariableDeclarationStatement -> bindVariableDeclarationStatement(statement as VariableDeclarationStatement)
            SyntaxType.IfStatement -> bindIfStatement(statement as IfStatement)
            SyntaxType.WhileStatement -> bindWhileStatement(statement as WhileStatement)
            SyntaxType.ForStatement -> bindForStatement(statement as ForStatement)
            else -> error("unknown statement type ${statement.type}")
        }
    }

    private fun bindBlockStatement(statement: BlockStatement): BoundBlockStatement {
        val blockBinder = createSubBinder()
        val statements = statement.statements.map { blockBinder.bindStatement(it) }
        return BoundBlockStatement(statements)
    }

    private fun bindExpressionStatement(statement: ExpressionStatement): BoundExpressionStatement {
        val expression = bindExpression(statement.expression)
        return BoundExpressionStatement(expression)
    }

    private fun bindVariableDeclarationStatement(statement: VariableDeclarationStatement): BoundVariableDeclarationStatement {
        val name = statement.identifier.text
        val isReadOnly = statement.keyword.type == SyntaxType.ValKeyword
        val initializer = bindExpression(statement.initializer)
        val variable = VariableSymbol(name, isReadOnly, initializer.type)

        if (!scope.declare(variable)) {
            diagnostics.variableAlreadyDeclared(statement.identifier.span, name)
        }

        return BoundVariableDeclarationStatement(variable, initializer)
    }

    private fun bindIfStatement(statement: IfStatement): BoundIfStatement {
        val condition = bindExpression(statement.condition, builtInTypeBoolean)
        val thenStatement = bindStatement(statement.thenStatement)
        val elseStatement = statement.elseClause?.statement?.let { bindStatement(it) }
        return BoundIfStatement(condition, thenStatement, elseStatement)
    }

    private fun bindWhileStatement(statement: WhileStatement): BoundWhileStatement {
        val condition = bindExpression(statement.condition, builtInTypeBoolean)
        val body = bindStatement(statement.body)
        return BoundWhileStatement(condition, body)
    }

    private fun bindForStatement(statement: ForStatement): BoundForStatement {
        val lowerBound = bindExpression(statement.lowerBound, builtInTypeInt)
        val upperBound = bindExpression(statement.upperBound, builtInTypeInt)

        val bodyBinder = createSubBinder()
        val name = statement.variable.text
        val variable = VariableSymbol(name, true, builtInTypeInt)
        if (!bodyBinder.scope.declare(variable)) {
            diagnostics.variableAlreadyDeclared(statement.variable.span, name)
        }

        val body = bodyBinder.bindStatement(statement.body)
        return BoundForStatement(variable, lowerBound, upperBound, body)
    }

    private fun bindExpression(expression: ExpressionNode, targetType: Type): BoundExpression {
        val result = bindExpression(expression)
        if (result.type != targetType) {
            diagnostics.cannotConvert(expression.span, result.type, targetType)
        }
        return result
    }

    private fun bindExpression(expression: ExpressionNode): BoundExpression {
        return when (expression.type) {
            SyntaxType.ParenthesizedExpression -> bindParameterizedExpression(expression as ParenthesizedExpression)
            SyntaxType.LiteralExpression -> bindLiteralExpression(expression as LiteralExpression)
            SyntaxType.UnaryExpression -> bindUnaryExpression(expression as UnaryExpression)
            SyntaxType.BinaryExpression -> bindBinaryExpression(expression as BinaryExpression)
            SyntaxType.NameExpression -> bindNameExpression(expression as NameExpression)
            SyntaxType.AssignmentExpression -> bindAssignmentExpression(expression as AssignmentExpression)
            else -> error("unknown expression type ${expression.type}")
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
        if (name.isEmpty()) {
            // This means the token was inserted by the parse. We already
            // reported an error, so we can just return an error expression.
            return BoundLiteralExpression(0, builtInTypeInt)
        }

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

        val variable = scope[name] ?: run {
            diagnostics.undefinedName(expression.identifier, name)
            return BoundLiteralExpression(0, builtInTypeInt)
        }

        if (variable.isReadOnly) {
            diagnostics.cannotAssign(expression.equalsToken, name)
        }

        if (variable.type != boundExpression.type) {
            diagnostics.incompatibleAssignment(expression.value.span, variable.type, boundExpression.type)
        }
        return BoundAssignmentExpression(variable, boundExpression)
    }

    private fun createSubBinder() = Binder(scope, diagnostics)

    companion object {
        fun bindGlobalScope(previous: BoundGlobalScope?, compilationUnit: CompilationUnit): BoundGlobalScope {
            val parentScope = createParentScopes(previous)
            val binder = Binder(parentScope)
            val statement = Lowerer.lower(binder.bindStatement(compilationUnit.statement))
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
    this += Diagnostic(operator.span, "unary operator '${operator.text}' is not defined for type '${type.name}'")
}

fun Diagnostics.unknownBinaryOperator(operator: Token, leftType: Type, rightType: Type) {
    this += Diagnostic(operator.span, "binary operator '${operator.text}' is not defined for types '${leftType.name}' and '${rightType.name}'")
}

fun Diagnostics.undefinedName(identifier: Token, name: CodePointString) {
    this += Diagnostic(identifier.span, "variable '$name' is not defined")
}

private fun Diagnostics.incompatibleAssignment(span: TextSpan, variableType: Type, expressionType: Type) {
    this += Diagnostic(span, "cannot assign expression of type '${expressionType.name}' to variable of type '${variableType.name}'")
}

private fun Diagnostics.variableAlreadyDeclared(span: TextSpan, name: CodePointString) {
    this += Diagnostic(span, "variable '$name' has already been declared")
}

private fun Diagnostics.cannotAssign(equalsToken: Token, name: CodePointString) {
    this += Diagnostic(equalsToken.span, "variable '$name' is read-only and cannot be reassigned")
}

private fun Diagnostics.cannotConvert(span: TextSpan, actualType: Type, expectedType: Type) {
    this += Diagnostic(span, "expected expression of type '${expectedType.name}' but got '${actualType.name}'")
}
