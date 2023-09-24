package de.cramer.compiler

import de.cramer.compiler.binding.VariableSymbol
import de.cramer.compiler.syntax.SyntaxTree
import de.cramer.compiler.syntax.writeTo
import de.cramer.compiler.text.SourceText
import de.cramer.compiler.text.TextSpan
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.ansi
import org.fusesource.jansi.AnsiConsole
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess

fun main() {
    System.setProperty(AnsiConsole.JANSI_MODE, AnsiConsole.JANSI_MODE_FORCE)
    AnsiConsole.systemInstall()

    try {
        acceptInputs()
    } finally {
        AnsiConsole.systemUninstall()
    }
}

@Suppress("NestedBlockDepth")
private fun acceptInputs() {
    val configuration = Configuration()
    val variables = mutableMapOf<VariableSymbol, Any>()
    val inputBuilder = StringBuilder()

    while (true) {
        if (inputBuilder.isEmpty()) printPrompt() else printContinuation()
        val line = readlnOrNull() ?: return

        val isBlank = line.isBlank()
        if (inputBuilder.isEmpty()) {
            if (isBlank) {
                inputBuilder.clear()
                continue
            }

            if (line.startsWith("#")) {
                runBuiltinFunction(line.substring(1), configuration)
                continue
            }
        }

        inputBuilder.appendLine(line)
        val input = inputBuilder.toString()

        try {
            val syntaxTree = SyntaxTree.parse(input)

            if (!isBlank && syntaxTree.diagnostics.isNotEmpty()) {
                continue
            }
            inputBuilder.clear()

            if (configuration.showTree) {
                print(ansi().fgBrightBlack())
                syntaxTree.root.writeTo(System.out)
                print(ansi().reset())
            }

            val compilation = Compilation(syntaxTree)
            when (val evaluationResult = compilation.evaluate(variables)) {
                is EvaluationResult.Success -> println(evaluationResult.value)
                is EvaluationResult.Failure -> printDiagnostics(evaluationResult.diagnostics, syntaxTree.text)
            }
        } catch (e: Exception) {
            print(ansi().startError())
            @Suppress("PrintStackTrace")
            e.printStackTrace(System.out)
            print(ansi().reset())
        }
    }
}

fun runBuiltinFunction(function: String, configuration: Configuration) {
    when (function) {
        "cls", "clear" -> println(ansi().eraseScreen())
        "exit", "q", "quit" -> exitProcess(0)
        "showTree" -> {
            configuration.showTree = !configuration.showTree
            if (configuration.showTree) {
                println("showing parse tree")
            } else {
                println("not showing parse tree")
            }
        }

        else -> println(ansi().error("unknown builtin function \"$function\""))
    }
}

fun printPrompt() {
    print(ansi().fgBrightBlue().a("> ").reset())
}

fun printContinuation() {
    print("| ")
}

private fun printDiagnostics(diagnostics: List<Diagnostic>, text: SourceText) {
    for (diagnostic in diagnostics) {
        val lineIndex = text.getLineIndex(diagnostic.span.start)
        val line = text.lines[lineIndex]
        val lineNumber = lineIndex + 1
        val character = diagnostic.span.start - line.span.start + 1

        println(ansi().error("($lineNumber, $character): ${diagnostic.message}"))

        val prefixSpan = TextSpan(line.span.start..<diagnostic.span.start)
        val suffixSpan = TextSpan(diagnostic.span.end..<line.span.end)

        val prefix = text.substring(prefixSpan)
        val error = text.substring(diagnostic.span)
        val suffix = text.substring(suffixSpan)

        print("    $prefix")
        when {
            error.isEmpty() && suffix.isEmpty() -> print(ansi().error("_"))
            error.isEmpty() -> {}
            error.toString().isBlank() -> print(ansi().error("_".repeat(error.length)))
            else -> print(ansi().error(error.toString()))
        }
        println(suffix)
    }
}

fun Ansi.startError(): Ansi = fgRed()

fun Ansi.error(error: String?): Ansi = startError().a(error).reset()

data class Configuration(
    var showTree: Boolean = false,
    var charset: Charset = StandardCharsets.UTF_8,
)
