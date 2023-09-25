package de.cramer.compiler.syntax

import de.cramer.compiler.text.TextSpan

data class Token(
    override val type: SyntaxType,
    override val span: TextSpan,
    val text: CodePointString,
    val value: Any?,
) : SyntaxNode {
    override val children: List<SyntaxNode>
        get() = emptyList()
}
