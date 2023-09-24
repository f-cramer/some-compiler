package de.cramer.compiler.syntax

class CodePointString internal constructor(
    private val codePoints: IntArray,
) {
    val length: Int
        get() = codePoints.size
    val string: String by lazy {
        codePoints.asSequence()
            .joinToString(separator = "") { Character.toString(it) }
    }

    fun isEmpty(): Boolean = length == 0

    operator fun get(codePointIndex: Int): CodePoint {
        return CodePoint(codePoints[codePointIndex])
    }

    fun substring(start: Int, end: Int): CodePointString =
        CodePointString(codePoints.copyOfRange(start, end))

    fun substring(range: IntRange): CodePointString =
        CodePointString(codePoints.copyOfRange(range.first, range.last + 1))

    override fun toString(): String = string

    override fun hashCode(): Int = string.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CodePointString

        return string == other.string
    }
}

fun List<CodePoint>.asCodePoints(): CodePointString = CodePointString(map { it.codePoint }.toIntArray())

fun String.asCodePoints(): CodePointString = CodePointString(codePoints().toArray())

val emptyCodePointString: CodePointString by lazy { CodePointString(intArrayOf()) }
