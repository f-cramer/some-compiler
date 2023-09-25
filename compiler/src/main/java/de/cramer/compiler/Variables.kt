package de.cramer.compiler

import de.cramer.compiler.binding.VariableSymbol
import java.util.IdentityHashMap

class Variables {
    private val delegate = IdentityHashMap<VariableSymbol, Any>()

    operator fun set(variable: VariableSymbol, value: Any) {
        delegate[variable] = value
    }

    operator fun get(variable: VariableSymbol): Any? = delegate[variable]
}
