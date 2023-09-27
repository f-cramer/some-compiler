package de.cramer.compiler.binding

import de.cramer.compiler.binding.TypeSymbol.Companion.boolean
import de.cramer.compiler.binding.TypeSymbol.Companion.int
import de.cramer.compiler.syntax.SyntaxType

val unaryOperatorIdentityInt = BoundUnaryOperator(BoundUnaryOperatorKind.Identity, SyntaxType.PlusToken, int)
val unaryOperatorNegationInt = BoundUnaryOperator(BoundUnaryOperatorKind.Negation, SyntaxType.MinusToken, int)
val unaryOperatorLogicalNegationBoolean = BoundUnaryOperator(BoundUnaryOperatorKind.LogicalNegation, SyntaxType.BangToken, boolean)
val unaryOperatorBitwiseComplementInt = BoundUnaryOperator(BoundUnaryOperatorKind.BitwiseComplement, SyntaxType.TildeToken, int)

val builtInUnaryOperators = listOf(
    unaryOperatorIdentityInt,
    unaryOperatorNegationInt,
    unaryOperatorLogicalNegationBoolean,
    unaryOperatorBitwiseComplementInt,
).groupBy { it.tokenType }

fun findBuiltInUnaryOperator(tokenType: SyntaxType, operandType: TypeSymbol): BoundUnaryOperator? {
    val operators = builtInUnaryOperators[tokenType] ?: return null
    return operators.find { it.operandTypeMatcher(operandType) }
}

data class BoundUnaryOperator(
    val kind: BoundUnaryOperatorKind,
    val tokenType: SyntaxType,
    val operandTypeMatcher: TypeMatcher,
    val type: TypeSymbol,
) {
    constructor(kind: BoundUnaryOperatorKind, tokenType: SyntaxType, type: TypeSymbol) :
        this(kind, tokenType, TypeMatcher(type), type)
}

enum class BoundUnaryOperatorKind {
    Identity,
    Negation,

    LogicalNegation,
    BitwiseComplement,
}
