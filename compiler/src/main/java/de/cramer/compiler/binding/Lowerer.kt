package de.cramer.compiler.binding

import java.util.ArrayDeque

class Lowerer private constructor() : BoundTreeRewriter() {

    private var numberOfLabels = 0

    private fun generateLabel(): LabelSymbol {
        numberOfLabels++
        return LabelSymbol("Label$numberOfLabels")
    }

    override fun rewriteIfStatement(statement: BoundIfStatement): BoundStatement = rewriteStatement(
        if (statement.elseStatement == null) {
            // if <condition>
            //      <then>
            //
            // ---->
            //
            // gotoFalse <condition> end
            // <then>
            // end:

            val endLabel = generateLabel()
            val gotoEnd = BoundConditionalGotoStatement(endLabel, statement.condition, false)
            val endLabelStatement = BoundLabelStatement(endLabel)
            BoundBlockStatement(listOf(gotoEnd, statement.thenStatement, endLabelStatement))
        } else {
            // if <condition>
            //      <then>
            // else
            //      <else>
            //
            // ---->
            //
            // gotoFalse <condition> else
            // <then>
            // goto end
            // else:
            // <else>
            // end:

            val elseLabel = generateLabel()
            val gotoElse = BoundConditionalGotoStatement(elseLabel, statement.condition, false)
            val endLabel = generateLabel()
            val gotoEnd = BoundGotoStatement(endLabel)
            val elseLabelStatement = BoundLabelStatement(elseLabel)
            val endLabelStatement = BoundLabelStatement(endLabel)
            BoundBlockStatement(listOf(gotoElse, statement.thenStatement, gotoEnd, elseLabelStatement, statement.elseStatement, endLabelStatement))
        },
    )

    override fun rewriteWhileStatement(statement: BoundWhileStatement): BoundStatement {
        // while <condition>
        //      <bode>
        //
        // ----->
        //
        // continue:
        // gotoFalse <condition> end
        // <body>
        // goto continue:
        // end:

        val continueLabel = generateLabel()
        val continueLabelStatement = BoundLabelStatement(continueLabel)
        val endLabel = generateLabel()
        val gotoEnd = BoundConditionalGotoStatement(endLabel, statement.condition, false)
        val gotoContinue = BoundGotoStatement(continueLabel)
        val endLabelStatement = BoundLabelStatement(endLabel)
        val block = BoundBlockStatement(listOf(continueLabelStatement, gotoEnd, statement.body, gotoContinue, endLabelStatement))
        return rewriteStatement(block)
    }

    override fun rewriteForStatement(statement: BoundForStatement): BoundStatement {
        // for <var> = <lower> to <upper>
        //      <body>
        //
        // ---->
        //
        // {
        //      var <var> = <lower>
        //      while (<var> <= <upper>)
        //      {
        //          <body>
        //          <var> = <var> + 1
        //      }
        // }

        val variableDeclaration = BoundVariableDeclarationStatement(statement.variable, statement.lowerBound)
        val variableExpression = BoundVariableExpression(statement.variable)
        val condition = BoundBinaryExpression(variableExpression, binaryOperatorLessOrEqualIntInt, statement.upperBound)
        val increment = BoundExpressionStatement(
            BoundAssignmentExpression(
                statement.variable,
                BoundBinaryExpression(
                    BoundVariableExpression(statement.variable),
                    binaryOperatorAdditionIntInt,
                    BoundLiteralExpression(1, builtInTypeInt),
                ),
            ),
        )
        val body = BoundBlockStatement(listOf(statement.body, increment))
        val whileStatement = BoundWhileStatement(condition, body)
        val block = BoundBlockStatement(listOf(variableDeclaration, whileStatement))
        return rewriteStatement(block)
    }

    companion object {
        fun lower(statement: BoundStatement): BoundBlockStatement {
            return Lowerer().rewriteStatement(statement).flatten()
        }

        private fun BoundStatement.flatten(): BoundBlockStatement {
            val statements = buildList {
                val stack = ArrayDeque<BoundStatement>()
                stack.push(this@flatten)

                while (stack.isNotEmpty()) {
                    val current = stack.pop()
                    if (current is BoundBlockStatement) {
                        for (statement in current.statements.asReversed()) {
                            stack.push(statement)
                        }
                    } else {
                        this += current
                    }
                }
            }

            return BoundBlockStatement(statements)
        }
    }
}
