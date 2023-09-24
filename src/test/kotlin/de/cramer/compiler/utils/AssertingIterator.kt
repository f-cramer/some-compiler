package de.cramer.compiler.utils

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import de.cramer.compiler.syntax.SyntaxNode
import de.cramer.compiler.syntax.SyntaxType
import de.cramer.compiler.syntax.Token
import de.cramer.compiler.syntax.assert
import java.util.ArrayDeque

class AssertingIterator private constructor(
    val iterator: Iterator<SyntaxNode>,
) : AutoCloseable {
    private var hasErrors = false

    constructor(node: SyntaxNode) : this(flatten(node))

    fun assertNode(type: SyntaxType) {
        interceptFailures {
            assertThat(iterator.hasNext(), name = "iterator has more elements").isTrue()
            val node = assertThat(iterator.next())
            node.isNotInstanceOf<Token>()
            node.prop(SyntaxNode::type).isEqualTo(type)
        }
    }

    fun assertToken(type: SyntaxType, text: String) {
        interceptFailures {
            assertThat(iterator.hasNext(), name = "iterator has more elements").isTrue()
            val token = assertThat(iterator.next()).isInstanceOf<Token>()
            token.assert(type, text)
        }
    }

    override fun close() {
        if (!hasErrors) {
            assertThat(!iterator.hasNext(), name = "iterator is done").isTrue()
        }
    }

    private inline fun interceptFailures(crossinline block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            hasErrors = true
            throw e
        }
    }

    companion object {
        private fun flatten(node: SyntaxNode): Iterator<SyntaxNode> = iterator {
            val stack = ArrayDeque<SyntaxNode>()
            stack.push(node)

            while (stack.size > 0) {
                val n = stack.pop()
                yield(n)

                for (child in n.children.asReversed()) {
                    stack.push(child)
                }
            }
        }
    }
}
