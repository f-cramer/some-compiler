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
    OpenBraceToken,
    CloseBraceToken,

    VarKeyword,
    ValKeyword,
    TrueKeyword,
    FalseKeyword,

    NameExpression,
    AssignmentExpression,

    LiteralExpression,
    UnaryExpression,
    BinaryExpression,
    ParenthesizedExpression,

    BlockStatement,
    ExpressionStatement,
    VariableDeclarationStatement,

    CompilationUnit,

    BadInputToken,
    EndOfFileToken,
}
