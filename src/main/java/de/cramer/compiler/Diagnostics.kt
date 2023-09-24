package de.cramer.compiler

import de.cramer.compiler.text.TextSpan
import java.io.Serial

class Diagnostics : ArrayList<Diagnostic>() {
    companion object {
        @Serial
        @Suppress("ConstPropertyName")
        private const val serialVersionUID: Long = 2585411350467585834L
    }
}

data class Diagnostic(
    val span: TextSpan,
    val message: String,
)
