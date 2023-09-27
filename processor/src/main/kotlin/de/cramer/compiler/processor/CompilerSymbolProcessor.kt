package de.cramer.compiler.processor

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import de.cramer.compiler.processor.extensions.isCollectionOf
import de.cramer.compiler.processor.extensions.isSubClassOf
import de.cramer.compiler.processor.extensions.suppressCompilerWarnings

private const val GET_CHILDREN_FUNCTION_NAME = "getChildren"

private const val SYNTAX_PACKAGE = "de.cramer.compiler.syntax"
private const val SYNTAX_NODE = "SyntaxNode"

class CompilerSymbolProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.createGetChildrenMethod(ClassName(SYNTAX_PACKAGE, SYNTAX_NODE), ClassName(SYNTAX_PACKAGE, "${SYNTAX_NODE}Children"), GET_CHILDREN_FUNCTION_NAME, EnumBasedDispatchCodeGenerator(ClassName(SYNTAX_PACKAGE, "SyntaxType"), "type"), GetChildrenInnerCodeGenerator)
        return emptyList()
    }

    private fun Resolver.createGetChildrenMethod(superClassName: ClassName, fileName: ClassName, functionName: String, dispatchCodeGenerator: DispatchCodeGenerator, innerCodeGenerator: InnerCodeGenerator) {
        val superClass = superClassName.canonicalName
        val newSubClasses = getSubClassesOf(superClass, true)
        if (newSubClasses.isEmpty()) return
        val allSubClasses = getSubClassesOf(superClass, false)
        val subClassesAndCodeBlocks = allSubClasses.associateWith {
            innerCodeGenerator(InnerCodeGeneratorArguments(it, superClassName, dispatchCodeGenerator.innerCodeNeedsTypeCast, functionName))
        }

        val dependencies = allSubClasses.mapNotNull { it.containingFile }.toTypedArray()

        val code = CodeBlock.builder()
            .add("return buildList {")
            .addStatement("")
            .indent()
            .apply { dispatchCodeGenerator(DispatchCodeGeneratorArguments(this, superClassName, subClassesAndCodeBlocks, functionName, this@createGetChildrenMethod)) }
            .unindent()
            .add("}")
            .build()

        val fileSpec = FunSpec.builder(functionName)
            .receiver(superClassName)
            .returns(List::class.asClassName().parameterizedBy(innerCodeGenerator.getType(superClassName)))
            .addCode(code)
            .build()
            .createFile(fileName)

        @Suppress("SpreadOperator")
        fileSpec.writeTo(codeGenerator, Dependencies(true, *dependencies))
    }

    private fun FunSpec.createFile(fileName: ClassName): FileSpec = FileSpec.builder(fileName.packageName, fileName.simpleName)
        .addFunction(this)
        .indent("    ")
        .suppressCompilerWarnings("RedundantVisibilityModifier")
        .build()

    private fun Resolver.getSubClassesOf(superClass: String, newFileOnly: Boolean): List<KSClassDeclaration> = (if (newFileOnly) getNewFiles() else getAllFiles())
        .flatMap { it.declarations }
        .filterIsInstance<KSClassDeclaration>()
        .filterNot { it.isAbstract() }
        .filter { c -> c.isSubClassOf(superClass) }
        .toList()

    private interface InnerCodeGenerator {
        fun getType(baseClass: ClassName): TypeName

        operator fun invoke(arguments: InnerCodeGeneratorArguments): CodeBlock
    }

    private data class InnerCodeGeneratorArguments(
        val clazz: KSClassDeclaration,
        val baseType: ClassName,
        val needsTypeCast: Boolean,
        val functionName: String,
    )

    private object GetChildrenInnerCodeGenerator : InnerCodeGenerator {
        override fun getType(baseClass: ClassName): TypeName = baseClass

        override fun invoke(arguments: InnerCodeGeneratorArguments): CodeBlock {
            val declaredProperties = arguments.clazz.getDeclaredProperties()
            val baseType = arguments.baseType
            val properties = declaredProperties
                .associate { it.simpleName.asString() to it.type.resolve() }
                .filterValues { (it.declaration as KSClassDeclaration).isSubClassOf(baseType.canonicalName) || it.isCollectionOf(baseType.canonicalName) }

            val codeBuilder = CodeBlock.builder()

            if (properties.isNotEmpty()) {
                if (arguments.needsTypeCast) {
                    codeBuilder.addStatement("this@%L as %T", arguments.functionName, arguments.clazz.toClassName())
                }
            }
            properties.forEach { (name, type) ->
                if (type.isMarkedNullable) {
                    codeBuilder.addStatement("this@%L.%L?.let { this += it }", arguments.functionName, name)
                } else {
                    codeBuilder.addStatement("this += this@%L.%L", arguments.functionName, name)
                }
            }
            return codeBuilder.build()
        }
    }

    private interface DispatchCodeGenerator {
        val innerCodeNeedsTypeCast: Boolean

        operator fun invoke(arguments: DispatchCodeGeneratorArguments)
    }

    private data class DispatchCodeGeneratorArguments(
        val code: CodeBlock.Builder,
        val superClass: ClassName,
        val subClasses: Map<KSClassDeclaration, CodeBlock>,
        val functionName: String,
        val resolver: Resolver,
    )

    private class EnumBasedDispatchCodeGenerator(
        private val enumClassName: ClassName,
        private val enumProperty: String,
    ) : DispatchCodeGenerator {
        override val innerCodeNeedsTypeCast: Boolean
            get() = true

        override fun invoke(arguments: DispatchCodeGeneratorArguments) {
            val defaultNodes = arguments.subClasses.keys.filter { c ->
                val annotationType = Default::class
                c.annotations.filter {
                    it.shortName.getShortName() == annotationType.simpleName && it.annotationType.resolve().declaration
                        .qualifiedName?.asString() == annotationType.qualifiedName
                }.any { annotation ->
                    annotation.arguments.any {
                        it.name?.getShortName() == "value" && (it.value as KSType).toClassName() == arguments.superClass
                    }
                }
            }
            if (defaultNodes.size > 1) {
                error("cannot have more then one default node, found ${defaultNodes.size}: $defaultNodes")
            }
            val defaultNode = defaultNodes.singleOrNull()

            val code = arguments.code
            code.beginControlFlow("when (%L)", enumProperty)
            arguments.subClasses.forEach { (it, innerBlock) ->
                if (it != defaultNode) {
                    val type = it.simpleName.asString()
                    code.add("%T.%L -> {\n", enumClassName, type)
                        .indent()
                        .add(innerBlock)
                        .unindent()
                        .add("}\n")
                }
            }

            if (defaultNode == null) {
                code.addStatement("else -> error(%P)", "cannot get children for node of $enumProperty \$$enumProperty")
            } else {
                code.add("else -> {\n")
                    .indent()
                    .add(arguments.subClasses[defaultNode]!!)
                    .unindent()
                    .add("}\n")
            }

            code.endControlFlow()
        }
    }
}
