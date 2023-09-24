package de.cramer.compiler.text

data class TextSpan(
    val start: Int,
    val length: Int,
) {
    constructor(range: IntRange) : this(range.first, range.last - range.first + 1)

    init {
        require(length >= 0) { "length cannot be less then zero" }
    }

    val end: Int
        get() = start + length

    val range: IntRange
        get() = start..<end
}
