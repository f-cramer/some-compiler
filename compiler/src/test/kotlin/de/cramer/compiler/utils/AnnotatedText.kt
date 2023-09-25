package de.cramer.compiler.utils

import de.cramer.compiler.syntax.CodePoint
import de.cramer.compiler.syntax.CodePointString
import de.cramer.compiler.syntax.asCodePoints
import de.cramer.compiler.text.TextSpan
import java.util.ArrayDeque

data class AnnotatedText(
    val text: CodePointString,
    val spans: List<TextSpan>,
) {
    companion object {
        operator fun invoke(text: String): AnnotatedText {
            val codePoints = mutableListOf<CodePoint>()
            val spans = mutableListOf<TextSpan>()
            val startStack = ArrayDeque<Int>()

            var position = 0

            for (codePoint in text.asCodePoints()) {
                when (codePoint.singleOrNull()) {
                    '[' -> startStack.push(position)
                    ']' -> {
                        if (startStack.isEmpty()) {
                            error("no matching '[' found at position $position")
                        }
                        spans += TextSpan(startStack.pop()..<position)
                    }

                    else -> {
                        position++
                        codePoints += codePoint
                    }
                }
            }

            if (startStack.isNotEmpty()) {
                error("no matching ']' found for '[' at positions $startStack")
            }

            return AnnotatedText(codePoints.asCodePoints(), spans)
        }
    }
}
