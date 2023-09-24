package de.cramer.compiler.syntax

import java.io.PrintStream

fun SyntaxNode.writeTo(writer: PrintStream) {
    prettyPrint(writer, this)
}

fun prettyPrint(writer: PrintStream, node: SyntaxNode, indent: String = "", isLast: Boolean = true) {
    val marker = if (isLast) "\\--" else "+--"

    writer.print(indent)
    writer.print(marker)
    writer.print(node.type)

    if (node is Token) {
        node.value?.let { writer.print(" $it") }
    }
    writer.println()

    val indentMarker = if (isLast) " " else "|"
    val childrenIndent = "$indent$indentMarker   "

    val lastChild = node.children.lastOrNull()
    for (child in node.children) {
        prettyPrint(writer, child, childrenIndent, child == lastChild)
    }
}
