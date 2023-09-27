package de.cramer.compiler.cli

import de.cramer.compiler.binding.BoundBinaryOperator
import de.cramer.compiler.binding.BoundExpression
import de.cramer.compiler.binding.BoundNode
import de.cramer.compiler.binding.BoundStatement
import de.cramer.compiler.binding.BoundUnaryOperator
import de.cramer.compiler.binding.LabelSymbol
import de.cramer.compiler.binding.TypeSymbol
import de.cramer.compiler.binding.VariableSymbol
import de.cramer.compiler.binding.getChildren
import de.cramer.compiler.binding.getProperties
import de.cramer.compiler.syntax.getText
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.ansi
import java.io.PrintStream

fun BoundNode.writeTo(writer: PrintStream) {
    prettyPrint(writer, this)
}

private fun prettyPrint(writer: PrintStream, node: BoundNode, indent: String = "", isLast: Boolean = true) {
    val writeToSystemOut = writer == System.out
    fun PrintStream.print(text: Any?, ansiConfiguration: Ansi.() -> Unit) {
        if (writeToSystemOut) {
            val ansi = ansi().also { it.ansiConfiguration() }
            print(ansi.a(text).reset())
        } else {
            print(text)
        }
    }

    val marker = if (isLast) "\\--" else "+--"

    writer.print("$indent$marker") { fgBrightBlack() }
    writer.print(node::class.simpleName) {
        when (node) {
            is BoundExpression -> fgBlue()
            is BoundStatement -> fgCyan()
            else -> fgYellow()
        }
    }

    node.getProperties().forEachIndexed { index, (name, value) ->
        if (index > 0) {
            writer.print(",") { fgBrightBlack() }
        }
        writer.print(" ")
        writer.print(name) { fgGreen() }
        writer.print(" = ") { fgBrightBlack() }
        writer.print(value?.asString()) { fgYellow() }
    }
    writer.println()

    val indentMarker = if (isLast) " " else "|"
    val childrenIndent = "$indent$indentMarker   "

    val children = node.getChildren()
    val lastChild = children.lastOrNull()
    for (child in children) {
        prettyPrint(writer, child, childrenIndent, child == lastChild)
    }
}

private fun Any.asString(): String = when (this) {
    is BoundUnaryOperator -> tokenType.getText() ?: ""
    is BoundBinaryOperator -> tokenType.getText() ?: ""
    is LabelSymbol -> name
    is String -> "\"$this\""
    is TypeSymbol -> name
    is VariableSymbol -> name.toString()
    else -> toString()
}
