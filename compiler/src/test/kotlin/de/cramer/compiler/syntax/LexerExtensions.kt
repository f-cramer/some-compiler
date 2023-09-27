package de.cramer.compiler.syntax

import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import assertk.assertions.toStringFun
import de.cramer.compiler.Diagnostic
import de.cramer.compiler.text.SourceText

fun lex(text: String) = lexWithDiagnostics(text).tokens

fun lexWithDiagnostics(text: String): LexingResult {
    val lexer = Lexer(SourceText(text))
    val tokens = lexer.lex().dropLastWhile { it.type == SyntaxType.EndOfFileToken }
    return LexingResult(tokens, lexer.diagnostics())
}

fun Assert<Token>.assert(type: SyntaxType, text: String) {
    prop(Token::type).isEqualTo(type)
    prop(Token::text).toStringFun().isEqualTo(text)
}

data class LexingResult(
    val tokens: List<Token>,
    val diagnostics: List<Diagnostic>,
)
