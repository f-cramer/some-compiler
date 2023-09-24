package de.cramer.compiler.syntax

import de.cramer.compiler.syntax.expression.ExpressionNode
import de.cramer.compiler.syntax.statement.ExpressionStatement
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class ParserTests {

    @ParameterizedTest
    @MethodSource("getUnaryOperatorPairs")
    fun `unary expression honors precedences`(unary: SyntaxType, binary: SyntaxType) {
        val unaryPrecedence = unary.getUnaryOperatorPrecendence()!!
        val binaryPrecedence = binary.getBinaryOperatorPrecendence()!!

        val unaryText = unary.getText()!!
        val binaryText = binary.getText()!!
        val text = "$unaryText a $binaryText b"
        val expression = parseExpression(text)

        if (unaryPrecedence >= binaryPrecedence) {
            AssertingIterator(expression).use {
                it.assertNode(SyntaxType.BinaryExpression)
                it.assertNode(SyntaxType.UnaryExpression)
                it.assertToken(unary, unaryText)
                it.assertNode(SyntaxType.NameExpression)
                it.assertToken(SyntaxType.IdentifierToken, "a")
                it.assertToken(binary, binaryText)
                it.assertNode(SyntaxType.NameExpression)
                it.assertToken(SyntaxType.IdentifierToken, "b")
            }
        } else {
            AssertingIterator(expression).use {
                it.assertNode(SyntaxType.UnaryExpression)
                it.assertToken(unary, unaryText)
                it.assertNode(SyntaxType.BinaryExpression)
                it.assertNode(SyntaxType.NameExpression)
                it.assertToken(SyntaxType.IdentifierToken, "a")
                it.assertToken(binary, binaryText)
                it.assertNode(SyntaxType.NameExpression)
                it.assertToken(SyntaxType.IdentifierToken, "b")
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getBinaryOperatorPairs")
    fun `binary expression honors precedences`(op1: SyntaxType, op2: SyntaxType) {
        val op1Precedence = op1.getBinaryOperatorPrecendence()!!
        val op2Precedence = op2.getBinaryOperatorPrecendence()!!

        val op1Text = op1.getText()!!
        val op2Text = op2.getText()!!
        val text = "a $op1Text b $op2Text c"
        val expression = parseExpression(text)

        if (op1Precedence >= op2Precedence) {
            AssertingIterator(expression).use {
                it.assertNode(SyntaxType.BinaryExpression)
                it.assertNode(SyntaxType.BinaryExpression)
                it.assertNode(SyntaxType.NameExpression)
                it.assertToken(SyntaxType.IdentifierToken, "a")
                it.assertToken(op1, op1Text)
                it.assertNode(SyntaxType.NameExpression)
                it.assertToken(SyntaxType.IdentifierToken, "b")
                it.assertToken(op2, op2Text)
                it.assertNode(SyntaxType.NameExpression)
                it.assertToken(SyntaxType.IdentifierToken, "c")
            }
        } else {
            AssertingIterator(expression).use {
                it.assertNode(SyntaxType.BinaryExpression)
                it.assertNode(SyntaxType.NameExpression)
                it.assertToken(SyntaxType.IdentifierToken, "a")
                it.assertToken(op1, op1Text)
                it.assertNode(SyntaxType.BinaryExpression)
                it.assertNode(SyntaxType.NameExpression)
                it.assertToken(SyntaxType.IdentifierToken, "b")
                it.assertToken(op2, op2Text)
                it.assertNode(SyntaxType.NameExpression)
                it.assertToken(SyntaxType.IdentifierToken, "c")
            }
        }
    }

    private fun parseExpression(text: String): ExpressionNode {
        val root = SyntaxTree.parse(text).root.statement
        return (root as ExpressionStatement).expression
    }

    companion object {
        @JvmStatic
        @Suppress("UnusedPrivateMember")
        private fun getUnaryOperatorPairs() = getUnaryOperators().flatMap { unary ->
            getBinaryOperators().map { binary -> Arguments.of(unary, binary) }
        }

        @JvmStatic
        @Suppress("UnusedPrivateMember")
        private fun getBinaryOperatorPairs() = getBinaryOperators().flatMap { outer ->
            getBinaryOperators().map { inner -> Arguments.of(outer, inner) }
        }

        private fun getUnaryOperators() = SyntaxType.entries.filter { it.getUnaryOperatorPrecendence() != null }

        private fun getBinaryOperators() = SyntaxType.entries.filter { it.getBinaryOperatorPrecendence() != null }
    }
}
