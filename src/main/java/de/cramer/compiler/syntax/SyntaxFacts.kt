package de.cramer.compiler.syntax

@Suppress("MagicNumber")
fun SyntaxType.getUnaryOperatorPrecendence() = when (this) {
    SyntaxType.PlusToken, SyntaxType.MinusToken, SyntaxType.BangToken -> 13
    else -> null
}

@Suppress("MagicNumber")
fun SyntaxType.getBinaryOperatorPrecendence() = when (this) {
    SyntaxType.AsteriskToken, SyntaxType.SlashToken -> 12
    SyntaxType.PlusToken, SyntaxType.MinusToken -> 11
    SyntaxType.EqualsEqualsToken, SyntaxType.BangEqualsToken -> 8
    SyntaxType.AmpersandToken -> 7
    SyntaxType.CircumflexToken -> 6
    SyntaxType.PipeToken -> 5
    SyntaxType.AmpersandAmpersandToken -> 4
    SyntaxType.PipePipeToken -> 3
    else -> null
}

fun SyntaxType.getText() = when (this) {
    SyntaxType.PlusToken -> "+"
    SyntaxType.MinusToken -> "-"
    SyntaxType.AsteriskToken -> "*"
    SyntaxType.SlashToken -> "/"
    SyntaxType.BangToken -> "!"
    SyntaxType.EqualsToken -> "="
    SyntaxType.AmpersandToken -> "&"
    SyntaxType.AmpersandAmpersandToken -> "&&"
    SyntaxType.PipeToken -> "|"
    SyntaxType.PipePipeToken -> "||"
    SyntaxType.CircumflexToken -> "^"
    SyntaxType.EqualsEqualsToken -> "=="
    SyntaxType.BangEqualsToken -> "!="
    SyntaxType.OpenParenthesisToken -> "("
    SyntaxType.CloseParenthesisToken -> ")"
    SyntaxType.OpenBraceToken -> "{"
    SyntaxType.CloseBraceToken -> "}"
    SyntaxType.TrueKeyword -> "true"
    SyntaxType.FalseKeyword -> "false"
    else -> null
}
