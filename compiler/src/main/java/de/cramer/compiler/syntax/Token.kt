package de.cramer.compiler.syntax

import de.cramer.compiler.processor.Default
import de.cramer.compiler.text.TextSpan

@Default(SyntaxNode::class)
data class Token(
    override val type: SyntaxType,
    override val span: TextSpan,
    val text: CodePointString,
    val value: Any?,
) : SyntaxNode
