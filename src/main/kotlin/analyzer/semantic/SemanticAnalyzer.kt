package analyzer.semantic

import analyzer.SyntaxResult
import analyzer.semantic.exceptions.MultiInitException
import analyzer.semantic.exceptions.VariableNotInitException
import analyzer.semantic.exceptions.VariableNotInitYetException
import analyzer.semantic.exceptions.VariableTypeCompatibilityException
import dictionary.Dictionary
import logger.Logger
import model.Types

class SemanticAnalyzer(
    private val logger: Logger,
    private val provider: SemanticFileProvider
) {

    fun run(syntax: SyntaxResult) {
        checkMultiInit(syntax)
        checkNotInit(syntax)
        checkTypeCompatibility(syntax)
        logger.info(Dictionary.semanticEnd())
    }

    private fun checkTypeCompatibility(syntax: SyntaxResult) {
        if (syntax.identifierTypes.values.all { it != Types.UNKNOWN.type }.not()) {
            throw VariableTypeCompatibilityException()
        }
        for (list in syntax.setTypes.values) {
            val type = list[0]
            if (list.all { it == type }.not()) {
                throw VariableTypeCompatibilityException()
            }
        }
    }

    private fun checkNotInit(syntax: SyntaxResult) {
        for (used in syntax.usedIdentifiers.withIndex()) {
            if (used.value == -1) continue // skip -1
            val index = syntax.newIdentifiers.indexOf(used.value)
            if (index == -1) {
                throw VariableNotInitException(provider.getIdentifier(used.value))
            }
            if (index > used.index) {
                throw VariableNotInitYetException(provider.getIdentifier(used.value))
            }
        }
    }

    private fun checkMultiInit(syntax: SyntaxResult) {
        val multiInit = syntax.newIdentifiers.filter { it != -1 }.toMutableList()
        val distinct = multiInit.distinct().toMutableList()
        distinct.forEach {
            multiInit.remove(it)
        }
        if (multiInit.isNotEmpty()) {
            throw MultiInitException(multiInit.joinToString { provider.getIdentifier(it) })
        }
    }
}