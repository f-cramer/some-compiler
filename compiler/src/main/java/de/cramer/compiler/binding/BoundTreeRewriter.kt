package de.cramer.compiler.binding

abstract class BoundTreeRewriter {

    protected open fun rewriteStatement(statement: BoundStatement): BoundStatement {
        return when (statement.kind) {
            BoundStatementKind.BlockStatement -> rewriteBlockStatement(statement as BoundBlockStatement)
            BoundStatementKind.ExpressionStatement -> rewriteExpressionStatement(statement as BoundExpressionStatement)
            BoundStatementKind.VariableDeclarationStatement -> rewriteVariableDeclarationStatement(statement as BoundVariableDeclarationStatement)
            BoundStatementKind.IfStatement -> rewriteIfStatement(statement as BoundIfStatement)
            BoundStatementKind.WhileStatement -> rewriteWhileStatement(statement as BoundWhileStatement)
            BoundStatementKind.ForStatement -> rewriteForStatement(statement as BoundForStatement)
            BoundStatementKind.LabelStatement -> rewriteLabelStatement(statement as BoundLabelStatement)
            BoundStatementKind.GotoStatement -> rewriteGotoStatement(statement as BoundGotoStatement)
            BoundStatementKind.ConditionalGotoStatement -> rewriteConditionalGotoStatement(statement as BoundConditionalGotoStatement)
        }
    }

    protected open fun rewriteBlockStatement(statement: BoundBlockStatement): BoundStatement {
        var statements: MutableList<BoundStatement>? = null
        for (i in statement.statements.indices) {
            val oldStatement = statement.statements[i]
            val newStatement = rewriteStatement(oldStatement)
            if (oldStatement != newStatement) {
                statements = mutableListOf()
                for (j in 0..<i) {
                    statements += statement.statements[j]
                }
            }

            if (statements != null) {
                statements += newStatement
            }
        }

        return if (statements != null) BoundBlockStatement(statements) else statement
    }

    protected open fun rewriteExpressionStatement(statement: BoundExpressionStatement): BoundStatement {
        val expression = rewriteExpression(statement.expression)
        if (expression == statement.expression) {
            return statement
        }

        return BoundExpressionStatement(expression)
    }

    protected open fun rewriteVariableDeclarationStatement(statement: BoundVariableDeclarationStatement): BoundStatement {
        val initialier = rewriteExpression(statement.initializer)
        if (initialier == statement.initializer) {
            return statement
        }

        return BoundVariableDeclarationStatement(statement.variable, initialier)
    }

    protected open fun rewriteIfStatement(statement: BoundIfStatement): BoundStatement {
        val condition = rewriteExpression(statement.condition)
        val thenStatement = rewriteStatement(statement.thenStatement)
        val elseStatement = statement.elseStatement?.let { rewriteStatement(it) }
        if (
            condition == statement.condition &&
            thenStatement == statement.thenStatement &&
            elseStatement == statement.elseStatement
        ) {
            return statement
        }

        return BoundIfStatement(condition, thenStatement, elseStatement)
    }

    protected open fun rewriteWhileStatement(statement: BoundWhileStatement): BoundStatement {
        val condition = rewriteExpression(statement.condition)
        val body = rewriteStatement(statement.body)
        if (
            condition == statement.condition &&
            body == statement.body
        ) {
            return statement
        }

        return BoundWhileStatement(condition, body)
    }

    protected open fun rewriteForStatement(statement: BoundForStatement): BoundStatement {
        val lowerBound = rewriteExpression(statement.lowerBound)
        val upperBound = rewriteExpression(statement.upperBound)
        val body = rewriteStatement(statement.body)
        if (
            lowerBound == statement.lowerBound &&
            upperBound == statement.upperBound &&
            body == statement.body
        ) {
            return statement
        }

        return BoundForStatement(statement.variable, lowerBound, upperBound, body)
    }

    private fun rewriteLabelStatement(statement: BoundLabelStatement): BoundStatement {
        return statement
    }

    private fun rewriteGotoStatement(statement: BoundGotoStatement): BoundStatement {
        return statement
    }

    private fun rewriteConditionalGotoStatement(statement: BoundConditionalGotoStatement): BoundStatement {
        val condition = rewriteExpression(statement.condition)
        if (condition == statement.condition) {
            return statement
        }

        return BoundConditionalGotoStatement(statement.label, condition, statement.jumpIf)
    }

    protected open fun rewriteExpression(expression: BoundExpression): BoundExpression {
        return when (expression.kind) {
            BoundExpressionKind.LiteralExpression -> rewriteLiteralExpression(expression as BoundLiteralExpression)
            BoundExpressionKind.UnaryExpression -> rewriteUnaryExpression(expression as BoundUnaryExpression)
            BoundExpressionKind.BinaryExpression -> rewriteBinaryExpression(expression as BoundBinaryExpression)
            BoundExpressionKind.VariableExpression -> rewriteVariableExpression(expression as BoundVariableExpression)
            BoundExpressionKind.AssignmentExpression -> rewriteAssignmentExpression(expression as BoundAssignmentExpression)
            BoundExpressionKind.ErrorExpression -> rewriteErrorExpression(expression as BoundErrorExpression)
        }
    }

    protected open fun rewriteLiteralExpression(expression: BoundLiteralExpression): BoundExpression {
        return expression
    }

    protected open fun rewriteUnaryExpression(expression: BoundUnaryExpression): BoundExpression {
        val operand = rewriteExpression(expression.operand)
        if (operand == expression.operand) {
            return expression
        }

        return BoundUnaryExpression(expression.operator, operand)
    }

    protected open fun rewriteBinaryExpression(expression: BoundBinaryExpression): BoundExpression {
        val left = rewriteExpression(expression.left)
        val right = rewriteExpression(expression.right)
        if (
            left == expression.left &&
            right == expression.right
        ) {
            return expression
        }

        return BoundBinaryExpression(left, expression.operator, right)
    }

    protected open fun rewriteVariableExpression(expression: BoundVariableExpression): BoundExpression {
        return expression
    }

    protected open fun rewriteAssignmentExpression(expression: BoundAssignmentExpression): BoundExpression {
        val exp = rewriteExpression(expression.expression)
        if (exp == expression.expression) {
            return expression
        }

        return BoundAssignmentExpression(expression.variable, exp)
    }

    protected open fun rewriteErrorExpression(expression: BoundErrorExpression): BoundExpression {
        return expression
    }
}
