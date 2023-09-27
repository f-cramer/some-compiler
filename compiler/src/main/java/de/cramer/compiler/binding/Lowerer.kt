package de.cramer.compiler.binding

class Lowerer private constructor() : BoundTreeRewriter() {

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
        fun lower(statement: BoundStatement): BoundStatement {
            return Lowerer().rewriteStatement(statement)
        }
    }
}
