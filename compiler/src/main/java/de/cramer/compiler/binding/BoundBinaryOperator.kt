package de.cramer.compiler.binding

import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.getText

val binaryOperatorAdditionIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.Addition, SyntaxType.PlusToken, builtInTypeInt)
val binaryOperatorSubtractionIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.Subtraction, SyntaxType.MinusToken, builtInTypeInt)
val binaryOperatorMultiplicationIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.Multiplication, SyntaxType.AsteriskToken, builtInTypeInt)
val binaryOperatorDivisionIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.Division, SyntaxType.SlashToken, builtInTypeInt)
val binaryOperatorLogicalAndBooleanBoolean = BoundBinaryOperator(BoundBinaryOperatorKind.LogicalAnd, SyntaxType.AmpersandAmpersandToken, builtInTypeBoolean)
val binaryOperatorLogicalOrBooleanBoolean = BoundBinaryOperator(BoundBinaryOperatorKind.LogicalOr, SyntaxType.PipePipeToken, builtInTypeBoolean)
val binaryOperatorBitwiseAndBooleanBoolean = BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseAnd, SyntaxType.AmpersandToken, builtInTypeBoolean)
val binaryOperatorBitwiseOrBooleanBoolean = BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseOr, SyntaxType.PipeToken, builtInTypeBoolean)
val binaryOperatorBitwiseXorBooleanBoolean = BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseXor, SyntaxType.CircumflexToken, builtInTypeBoolean)
val binaryOperatorBitwiseAndIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseAnd, SyntaxType.AmpersandToken, builtInTypeInt)
val binaryOperatorBitwiseOrIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseOr, SyntaxType.PipeToken, builtInTypeInt)
val binaryOperatorBitwiseXorIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.BitwiseXor, SyntaxType.CircumflexToken, builtInTypeInt)
val binaryOperatorEqualsAny = BoundBinaryOperator(BoundBinaryOperatorKind.Equals, SyntaxType.EqualsEqualsToken, anyTypeMatcher, anyTypeMatcher, builtInTypeBoolean)
val binaryOperatorNotEqualsAny = BoundBinaryOperator(BoundBinaryOperatorKind.NotEquals, SyntaxType.BangEqualsToken, anyTypeMatcher, anyTypeMatcher, builtInTypeBoolean)
val binaryOperatorAdditionStringString = BoundBinaryOperator(BoundBinaryOperatorKind.Addition, SyntaxType.PlusToken, builtInTypeString)
val binaryOperatorLessIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.Less, SyntaxType.LessToken, builtInTypeInt, builtInTypeBoolean)
val binaryOperatorLessOrEqualIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.LessOrEqual, SyntaxType.LessOrEqualToken, builtInTypeInt, builtInTypeBoolean)
val binaryOperatorGreaterIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.Greater, SyntaxType.GreaterToken, builtInTypeInt, builtInTypeBoolean)
val binaryOperatorGreaterOrEqualIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.GreaterOrEqual, SyntaxType.GreaterOrEqualToken, builtInTypeInt, builtInTypeBoolean)

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
