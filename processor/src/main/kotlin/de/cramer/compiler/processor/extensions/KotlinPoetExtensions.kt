package de.cramer.compiler.processor.extensions

import com.squareup.kotlinpoet.Annotatable
import com.squareup.kotlinpoet.AnnotationSpec

internal fun <T : Annotatable.Builder<T>> T.suppressCompilerWarnings(vararg warnings: String): T {
    val annotation = AnnotationSpec.builder(Suppress::class)
        .addMember("%S, ".repeat(warnings.size).trimEnd(' ', ','), *warnings)
        .build()
    return addAnnotation(annotation)
}
