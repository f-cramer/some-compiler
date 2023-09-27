package de.cramer.compiler.syntax

import de.cramer.compiler.text.TextSpan

interface SyntaxNode {

    val type: SyntaxType
    val span: TextSpan
        get() {
            val children = getChildren()
            return TextSpan(children.first().span.start..<children.last().span.end)
        }
}
