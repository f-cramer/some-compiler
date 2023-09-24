package de.cramer.compiler.binding

import de.cramer.compiler.syntax.SyntaxType

val binaryOperatorAdditionIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.Addition, SyntaxType.PlusToken, builtInTypeInt)
val binaryOperatorSubtractionIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.Subtraction, SyntaxType.MinusToken, builtInTypeInt)
val binaryOperatorMultiplicationIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.Multiplication, SyntaxType.AsteriskToken, builtInTypeInt)
val binaryOperatorDivisionIntInt = BoundBinaryOperator(BoundBinaryOperatorKind.Division, SyntaxType.SlashToken, builtInTypeInt)
val binaryOperatorLogicalAndBooleanBoolean = BoundBinaryOperator(BoundBinaryOperatorKind.LogicalAnd, SyntaxType.AmpersandAmpersandToken, builtInTypeBoolean)
val binaryOperatorLogicalOrBooleanBoolean = BoundBinaryOperator(BoundBinaryOperatorKind.LogicalAnd, SyntaxType.PipePipeToken, builtInTypeBoolean)
val binaryOperatorLogicalXorBooleanBoolean = BoundBinaryOperator(BoundBinaryOperatorKind.LogicalAnd, SyntaxType.CircumflexToken, builtInTypeBoolean)
val binaryOperatorEqualsAny = BoundBinaryOperator(BoundBinaryOperatorKind.Equals, SyntaxType.EqualsEqualsToken, anyTypeMatcher, anyTypeMatcher, builtInTypeBoolean)
val binaryOperatorNotEqualsAny = BoundBinaryOperator(BoundBinaryOperatorKind.NotEquals, SyntaxType.BangEqualsToken, anyTypeMatcher, anyTypeMatcher, builtInTypeBoolean)
val binaryOperatorAdditionStringString = BoundBinaryOperator(BoundBinaryOperatorKind.Addition, SyntaxType.PlusToken, builtInTypeString)

private val builtInBinaryOperators = listOf(
    binaryOperatorAdditionIntInt,
    binaryOperatorSubtractionIntInt,
    binaryOperatorMultiplicationIntInt,
    binaryOperatorDivisionIntInt,
    binaryOperatorLogicalAndBooleanBoolean,
    binaryOperatorLogicalOrBooleanBoolean,
    binaryOperatorLogicalXorBooleanBoolean,
    binaryOperatorEqualsAny,
    binaryOperatorNotEqualsAny,
    binaryOperatorAdditionStringString,
).groupBy { it.tokenType }

fun findBuiltInBinaryOperator(tokenType: SyntaxType, leftOperandType: Type, rightOperandType: Type): BoundBinaryOperator? {
    val operators = builtInBinaryOperators[tokenType] ?: return null
    return operators.find { it.leftOperandTypeMatcher(leftOperandType) && it.rightOperandTypeMatcher(rightOperandType) }
}

data class BoundBinaryOperator(
    val kind: BoundBinaryOperatorKind,
    val tokenType: SyntaxType,
    val leftOperandTypeMatcher: TypeMatcher,
    val rightOperandTypeMatcher: TypeMatcher,
    val type: Type,
) {
    constructor(kind: BoundBinaryOperatorKind, tokenType: SyntaxType, type: Type) :
        this(kind, tokenType, TypeMatcher(type), TypeMatcher(type), type)
}

enum class BoundBinaryOperatorKind {
    Addition,
    Subtraction,
    Multiplication,
    Division,

    LogicalAnd,
    LogicalOr,
    LogicalXor,

    Equals,
    NotEquals,
}
