package de.cramer.compiler.syntax

enum class SyntaxType {
    StringToken,
    WhitespaceToken,
    NumberToken,
    PlusToken,
    MinusToken,
    AsteriskToken,
    SlashToken,
    AmpersandToken,
    AmpersandAmpersandToken,
    PipeToken,
    PipePipeToken,
    CircumflexToken,
    EqualsToken,
    EqualsEqualsToken,
    BangToken,
    BangEqualsToken,
    IdentifierToken,

    OpenParenthesisToken,
    CloseParenthesisToken,
    OpenBracketToken,
    CloseBracketToken,

    TrueKeyword,
    FalseKeyword,

    NameExpression,
    AssignmentExpression,

    LiteralExpression,
    UnaryExpression,
    BinaryExpression,
    ParenthesizedExpression,

    BadInputToken,
    EndOfFileToken,
}
