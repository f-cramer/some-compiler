package de.cramer.compiler.binding

import de.cramer.compiler.binding.TypeSymbol.Companion.boolean
import de.cramer.compiler.binding.TypeSymbol.Companion.int
import de.cramer.compiler.binding.TypeSymbol.Companion.string
import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.getText

val binaryOperatorAdditionIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.Addition, SyntaxType.PlusToken, int)
val binaryOperatorSubtractionIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.Subtraction, SyntaxType.MinusToken, int)
val binaryOperatorMultiplicationIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.Multiplication, SyntaxType.AsteriskToken, int)
val binaryOperatorDivisionIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.Division, SyntaxType.SlashToken, int)
val binaryOperatorLogicalAndBooleanBoolean = BoundBinaryOperator(BoundBinaryOperatorKind.LogicalAnd, SyntaxType.AmpersandAmpersandToken, boolean)
val binaryOperatorLogicalOrBooleanBoolean = BoundBinaryOperator(BoundBinaryOperatorKind.LogicalOr, SyntaxType.PipePipeToken, boolean)
val binaryOperatorBitwiseAndBooleanBoolean = BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseAnd, SyntaxType.AmpersandToken, boolean)
val binaryOperatorBitwiseOrBooleanBoolean = BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseOr, SyntaxType.PipeToken, boolean)
val binaryOperatorBitwiseXorBooleanBoolean = BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseXor, SyntaxType.CircumflexToken, boolean)
val binaryOperatorBitwiseAndIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseAnd, SyntaxType.AmpersandToken, int)
val binaryOperatorBitwiseOrIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseOr, SyntaxType.PipeToken, int)
val binaryOperatorBitwiseXorIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseXor, SyntaxType.CircumflexToken, int)
val binaryOperatorEqualsAny = BoundBinaryOperator(BoundBinaryOperatorKind.Equals, SyntaxType.EqualsEqualsToken, anyTypeMatcher, anyTypeMatcher, boolean)
val binaryOperatorNotEqualsAny = BoundBinaryOperator(BoundBinaryOperatorKind.NotEquals, SyntaxType.BangEqualsToken, anyTypeMatcher, anyTypeMatcher, boolean)
val binaryOperatorAdditionStringString = BoundBinaryOperator(BoundBinaryOperatorKind.Addition, SyntaxType.PlusToken, string)
val binaryOperatorLessIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.Less, SyntaxType.LessToken, int, boolean)
val binaryOperatorLessOrEqualIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.LessOrEqual, SyntaxType.LessOrEqualToken, int, boolean)
val binaryOperatorGreaterIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.Greater, SyntaxType.GreaterToken, int, boolean)
val binaryOperatorGreaterOrEqualIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.GreaterOrEqual, SyntaxType.GreaterOrEqualToken, int, boolean)

private val builtInBinaryOperators = listOf(
    binaryOperatorAdditionIntInt,
    binaryOperatorSubtractionIntInt,
    binaryOperatorMultiplicationIntInt,
    binaryOperatorDivisionIntInt,
    binaryOperatorLogicalAndBooleanBoolean,
    binaryOperatorLogicalOrBooleanBoolean,
    binaryOperatorBitwiseAndBooleanBoolean,
    binaryOperatorBitwiseOrBooleanBoolean,
    binaryOperatorBitwiseXorBooleanBoolean,
    binaryOperatorBitwiseAndIntInt,
    binaryOperatorBitwiseOrIntInt,
    binaryOperatorBitwiseXorIntInt,
    binaryOperatorEqualsAny,
    binaryOperatorNotEqualsAny,
    binaryOperatorAdditionStringString,
    binaryOperatorLessIntInt,
    binaryOperatorLessOrEqualIntInt,
    binaryOperatorGreaterIntInt,
    binaryOperatorGreaterOrEqualIntInt,
).groupBy { it.tokenType }

fun findBuiltInBinaryOperator(tokenType: SyntaxType, leftOperandType: TypeSymbol, rightOperandType: TypeSymbol): BoundBinaryOperator? {
    val operators = builtInBinaryOperators[tokenType] ?: return null
    return operators.find { it.leftOperandTypeMatcher(leftOperandType) && it.rightOperandTypeMatcher(rightOperandType) }
}

data class BoundBinaryOperator(
    val kind: BoundBinaryOperatorKind,
    val tokenType: SyntaxType,
    val leftOperandTypeMatcher: TypeMatcher,
    val rightOperandTypeMatcher: TypeMatcher,
    val type: TypeSymbol,
) {
    constructor(kind: BoundBinaryOperatorKind, tokenType: SyntaxType, type: TypeSymbol) :
        this(kind, tokenType, TypeMatcher(type), TypeMatcher(type), type)

    constructor(kind: BoundBinaryOperatorKind, tokenType: SyntaxType, operandType: TypeSymbol, type: TypeSymbol) :
        this(kind, tokenType, TypeMatcher(operandType), TypeMatcher(operandType), type)

    override fun toString(): String = tokenType.getText() ?: ""
}

enum class BoundBinaryOperatorKind {
    Addition,
    Subtraction,
    Multiplication,
    Division,

    LogicalAnd,
    LogicalOr,
    BitwiseAnd,
    BitwiseOr,
    BitwiseXor,

    Equals,
    NotEquals,

    Less,
    LessOrEqual,
    Greater,
    GreaterOrEqual,
}
