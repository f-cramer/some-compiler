package de.cramer.compiler.syntax

import de.cramer.compiler.text.TextSpan

interface SyntaxNode {

    val type: SyntaxType
    val children: List<SyntaxNode>
    val span: TextSpan
        get() = TextSpan(children.first().span.start..<children.last().span.end)
}
