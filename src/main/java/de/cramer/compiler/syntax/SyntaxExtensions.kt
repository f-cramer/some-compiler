package de.cramer.compiler.syntax

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.ansi
import java.io.PrintStream

fun SyntaxNode.writeTo(writer: PrintStream) {
    prettyPrint(writer, this)
}

fun prettyPrint(writer: PrintStream, node: SyntaxNode, indent: String = "", isLast: Boolean = true) {
    val writeToSystemOut = writer == System.out
    fun PrintStream.print(text: Any, ansiConfiguration: Ansi.() -> Unit) {
        if (writeToSystemOut) {
            val ansi = ansi().also { it.ansiConfiguration() }
            print(ansi.a(text).reset())
        } else {
            print(text)
        }
    }

    val marker = if (isLast) "\\--" else "+--"

    writer.print("$indent$marker") { fgBrightBlack() }
    writer.print(node.type) { if (node is Token) fgBlue() else fgCyan() }

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
