package de.cramer.compiler.processor.extensions

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Variance

internal val KSDeclaration.name: KSName
    get() = qualifiedName ?: simpleName

internal fun KSDeclaration.isSealed() = Modifier.SEALED in modifiers

internal fun KSDeclaration.isSubClassOf(superClass: String): Boolean = this !is KSClassDeclaration || superTypes
    .map { it.resolve().declaration }
    .filterIsInstance<KSClassDeclaration>()
    .any { it.qualifiedName?.asString() == superClass || it.isSubClassOf(superClass) }

internal fun KSType.isCollectionOf(componentType: String): Boolean {
    val declaration = declaration as? KSClassDeclaration ?: return false
    if (!declaration.isSupportedCollectionType()) return false
    val argument = arguments.single()
    if (argument.variance != Variance.INVARIANT && argument.variance != Variance.COVARIANT) return false
    val argumentTypeDeclaration = argument.type?.resolve()?.declaration as? KSClassDeclaration ?: return false

    return argumentTypeDeclaration.isSubClassOf(componentType)
}

private fun KSClassDeclaration.isSupportedCollectionType(): Boolean =
    name.asString() in setOf("kotlin.collections.List", "kotlin.collections.Set")
