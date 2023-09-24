package de.cramer.compiler.syntax

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.prop
import de.cramer.compiler.text.SourceText
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class SourceTextTests {

    @ParameterizedTest
    @MethodSource("getTexts")
    fun `source text has correct number of lines`(text: String, expectedLineCount: Int) {
        val sourceText = SourceText(text)
        assertThat(sourceText).prop(SourceText::lines).hasSize(expectedLineCount)
    }

    companion object {
        @JvmStatic
        fun getTexts() = listOf(
            Arguments.of(".", 1),
            Arguments.of(".\r\n", 2),
            Arguments.of(".\n", 2),
            Arguments.of(".\r", 2),
            Arguments.of(".\r\n\r\n", 3),
            Arguments.of(".\n\n", 3),
            Arguments.of(".\n\r\n", 3),
            Arguments.of(".\r\r", 3),
            Arguments.of(".\r\r\n", 3),
        )
    }
}
