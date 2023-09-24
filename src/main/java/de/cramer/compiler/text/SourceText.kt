package de.cramer.compiler.text

class SourceText(
    private val text: String,
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

    override fun toString() = text

    fun substring(start: Int, end: Int) = text.substring(start, end)

    fun substring(span: TextSpan) = text.substring(span.range)

    companion object {
        private fun parseLines(sourceText: SourceText, text: String): List<TextLine> {
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

        private fun getLineBreakWidth(text: String, i: Int): Int {
            val c = text[i]
            val l = if (i + 1 >= text.length) '\u0000' else text[i + 1]

            return when {
                c == '\r' && l == '\n' -> 2
                c == '\r' || c == '\n' -> 1
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
