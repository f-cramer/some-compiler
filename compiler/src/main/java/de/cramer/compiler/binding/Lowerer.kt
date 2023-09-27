package de.cramer.compiler.binding

class Lowerer private constructor() : BoundTreeRewriter() {

    companion object {
        fun lower(statement: BoundStatement): BoundStatement {
            return Lowerer().rewriteStatement(statement)
        }
    }
}
