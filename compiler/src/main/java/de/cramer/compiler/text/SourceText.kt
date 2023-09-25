package de.cramer.compiler.text

import de.cramer.compiler.syntax.CodePoint
import de.cramer.compiler.syntax.CodePointString
import de.cramer.compiler.syntax.asCodePoints

class SourceText(
    private val text: CodePointString,
) {
    val length: Int = text.length

    val lines: List<TextLine> = parseLines(this, text)

    operator fun get(index: Int) = text[index]

    fun getLineIndex(position: Int): Int {
        var lower = 0
        var upper = lines.size - 1

        while (lower <= upper) {
            val index = lower + (upper - lower) / 2
            val start = lines[index].span.start

            if (position == start) {
                return index
            }

            if (start > position) {
                upper = index - 1
            } else {
                lower = index + 1
            }
        }

        return lower - 1
    }

    override fun toString() = text.toString()

    fun substring(start: Int, end: Int) = text.substring(start, end)

    fun substring(span: TextSpan) = text.substring(span.range)

    constructor(text: String) : this(text.asCodePoints())

    companion object {
        private fun parseLines(sourceText: SourceText, text: CodePointString): List<TextLine> {
            var position = 0
            var lineStart = 0

            return buildList {
                while (position < text.length) {
                    val lineBreakWidth = getLineBreakWidth(text, position)
                    if (lineBreakWidth == 0) {
                        position++
                    } else {
                        this += createLine(sourceText, position, lineStart, lineBreakWidth)
                        position += lineBreakWidth
                        lineStart = position
                    }
                }

                this += createLine(sourceText, position, lineStart, 0)
            }
        }

        private fun createLine(sourceText: SourceText, position: Int, lineStart: Int, lineBreakWidth: Int): TextLine {
            val lineLength = position - lineStart
            val lineLengthIncludingLineBreak = lineLength + lineBreakWidth
            return TextLine(sourceText, lineStart, lineLength, lineLengthIncludingLineBreak)
        }

        private fun getLineBreakWidth(text: CodePointString, i: Int): Int {
            val c = text[i]
            val l = if (i + 1 >= text.length) CodePoint(0) else text[i + 1]

            return when {
                c.isEqualTo('\r') && l.isEqualTo('\n') -> 2
                c.isEqualTo('\r') || c.isEqualTo('\n') -> 1
                else -> 0
            }
        }
    }
}

class TextLine(
    val text: SourceText,
    start: Int,
    length: Int,
    lengthIncludingLineBreak: Int,
) {
    val span = TextSpan(start, length)
    val spanIncludingLineBreak = TextSpan(start, lengthIncludingLineBreak)
}
