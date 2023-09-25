package de.cramer.compiler.syntax

import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import assertk.assertions.toStringFun
import de.cramer.compiler.text.SourceText

fun lex(text: String) = Lexer(SourceText(text)).lex().dropLastWhile { it.type == SyntaxType.EndOfFileToken }

fun Assert<Token>.assert(type: SyntaxType, text: String) {
    prop(Token::type).isEqualTo(type)
    prop(Token::text).toStringFun().isEqualTo(text)
}
