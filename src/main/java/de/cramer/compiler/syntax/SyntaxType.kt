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
    LessToken,
    LessOrEqualToken,
    GreaterToken,
    GreaterOrEqualToken,
    IdentifierToken,

    OpenParenthesisToken,
    CloseParenthesisToken,
    OpenBraceToken,
    CloseBraceToken,

    VarKeyword,
    ValKeyword,
    TrueKeyword,
    FalseKeyword,
    IfKeyword,
    ElseKeyword,
    WhileKeyword,

    NameExpression,
    AssignmentExpression,

    LiteralExpression,
    UnaryExpression,
    BinaryExpression,
    ParenthesizedExpression,

    BlockStatement,
    ExpressionStatement,
    VariableDeclarationStatement,
    IfStatement,
    ElseClause,
    WhileStatement,

    CompilationUnit,

    BadInputToken,
    EndOfFileToken,
}
