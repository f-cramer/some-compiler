package de.cramer.compiler.syntax

@JvmInline
value class CodePoint(
    val codePoint: Int,
) {
    fun isEqualTo(char: Char): Boolean =
        char.code == codePoint

    fun isDigit(): Boolean = singleChar { it.isDigit() }

    fun isWhitespace(): Boolean = singleChar { it.isWhitespace() }

    fun singleOrNull(): Char? =
        Character.toChars(codePoint).singleOrNull()

    fun singleChar(predicate: (Char) -> Boolean): Boolean {
        val chars = Character.toChars(codePoint)
        return chars.size == 1 && predicate(chars[0])
    }

    override fun toString(): String = Character.toString(codePoint)
}

fun CodePoint?.isEqualTo(char: Char): Boolean = this != null && this.isEqualTo(char)

fun Char.asCodePoint(): CodePoint = CodePoint(code)
