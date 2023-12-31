package de.cramer.compiler.syntax

import de.cramer.compiler.Diagnostic
import de.cramer.compiler.text.SourceText

data class SyntaxTree(
    val text: SourceText,
    val diagnostics: List<Diagnostic>,
    val root: CompilationUnit,
) {
    companion object {
        fun parse(text: String) = parse(SourceText(text))

        fun parse(text: SourceText): SyntaxTree {
            val parser = Parser(text)
            val compilationUnit = parser.parse()
            return SyntaxTree(text, parser.diagnostics(), compilationUnit)
        }
    }
}
