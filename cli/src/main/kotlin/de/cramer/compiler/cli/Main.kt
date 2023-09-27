package de.cramer.compiler.cli

import de.cramer.compiler.Compilation
import de.cramer.compiler.Diagnostic
import de.cramer.compiler.EvaluationResult
import de.cramer.compiler.Variables
import de.cramer.compiler.syntax.SyntaxTree
import de.cramer.compiler.text.SourceText
import de.cramer.compiler.text.TextSpan
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.Ansi.ansi
import org.fusesource.jansi.AnsiConsole
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.charset.UnsupportedCharsetException
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    System.setProperty(AnsiConsole.JANSI_MODE, AnsiConsole.JANSI_MODE_FORCE)
    AnsiConsole.systemInstall()

    try {
        val configuration = Configuration(args)
        acceptInputs(configuration)
    } finally {
        AnsiConsole.systemUninstall()
    }
}

@Suppress("NestedBlockDepth")
private fun acceptInputs(configuration: Configuration) {
    val variables = Variables()
    val inputBuilder = StringBuilder()

    while (true) {
        printPrompt(inputBuilder.isNotEmpty())
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
                syntaxTree.root.statement.writeTo(System.out)
            }

            val compilation = Compilation(configuration.previousCompilation, syntaxTree)
            if (configuration.showProgram) {
                compilation.getRoot().writeTo(System.out)
            }

            when (val evaluationResult = compilation.evaluate(variables)) {
                is EvaluationResult.Success -> {
                    evaluationResult.value?.let { println(ansi().fgMagenta().a(it).reset()) }
                    configuration.previousCompilation = compilation
                }

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
        "cls", "clear" -> runBuiltinFunctionClear()
        "exit", "q", "quit" -> runBuiltinFunctionExit()
        "reset" -> runBuiltinFunctionReset(configuration)
        "showProgram" -> runBuiltinFunctionShowProgram(configuration)
        "showTree" -> runBuiltinFunctionShowTree(configuration)
        else -> println(ansi().error("unknown builtin function \"$function\""))
    }
}

private fun runBuiltinFunctionClear() {
    println(ansi().eraseScreen())
}

private fun runBuiltinFunctionExit() {
    exitProcess(0)
}

fun runBuiltinFunctionReset(configuration: Configuration) {
    configuration.previousCompilation = null
    println("scope was reset")
}

private fun runBuiltinFunctionShowProgram(configuration: Configuration) {
    configuration.showProgram = !configuration.showProgram
    if (configuration.showProgram) {
        println("showing bound tree")
    } else {
        println("not showing bound tree")
    }
}

private fun runBuiltinFunctionShowTree(configuration: Configuration) {
    configuration.showTree = !configuration.showTree
    if (configuration.showTree) {
        println("showing parse tree")
    } else {
        println("not showing parse tree")
    }
}

fun printPrompt(continuation: Boolean) {
    val char = if (continuation) '|' else '>'
    print(ansi().fgBrightGreen().a("$char ").reset())
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
    var showProgram: Boolean = false,
    var showTree: Boolean = false,
    var charset: Charset = StandardCharsets.UTF_8,
    var previousCompilation: Compilation? = null,
) {
    constructor(args: Array<String>) : this("--showProgram" in args, "--showTree" in args) {
        val charsetArg = "--charset"
        args.lastOrNull { it.startsWith("$charsetArg=") }
            ?.substring(charsetArg.length + 1)
            ?.takeUnless { it.isBlank() }
            ?.let {
                try {
                    charset = Charset.forName(it)
                } catch (@Suppress("SwallowedException") e: UnsupportedCharsetException) {
                    println(ansi().error("Charset '$it' is not supported"))
                    exitProcess(1)
                }
            }
    }
}
