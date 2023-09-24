package de.cramer.compiler.syntax

import assertk.assertThat
import assertk.assertions.single
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class SyntaxFactsTests {

    @ParameterizedTest
    @MethodSource("getSyntaxTypes")
    fun `getText round trips`(type: SyntaxType) {
        val text = type.getText() ?: return
        val token = assertThat(lex(text)).single()
        token.assert(type, text)
    }

    companion object {
        @JvmStatic
        fun getSyntaxTypes() = SyntaxType.entries.toList()
    }
}
