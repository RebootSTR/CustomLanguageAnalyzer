package analyzer.syntax

import model.Grammar
import kotlin.reflect.KFunction3

object Utils {

    fun getTypesWords(): List<String> {
        return listOf(
            "integer",
            "real",
            "boolean"
        )
    }

    fun getLogicWords(): List<String> {
        return listOf(
            "true",
            "false",
        )
    }

    fun getOtnoshenieSigns(): List<String> {
        return listOf(
            "!=",
            "==",
            "<",
            "<=",
            ">",
            ">=",
        )
    }

    fun getSumSigns(): List<String> {
        return listOf(
            "+",
            "-",
            "||",
        )
    }

    fun getMultiplySigns(): List<String> {
        return listOf(
            "*",
            "/",
            "&&",
        )
    }

    fun getLineEnds(): List<String> {
        return listOf(
            ":",
            "\n"
        )
    }

    fun Grammar.or(
        strings: List<String>,
        provider: SyntaxFileProvider,
        checker: KFunction3<Grammar, String, SyntaxFileProvider, Boolean>
    ): Boolean {
        for (string in strings) {
            if (checker(this, string, provider)) {
                return true
            }
        }
        return false
    }

    fun Grammar.and(
        strings: List<String>,
        provider: SyntaxFileProvider,
        checker: KFunction3<Grammar, String, SyntaxFileProvider, Boolean>
    ): Boolean {
        for (string in strings) {
            if (!checker(this, string, provider)) {
                return false
            }
        }
        return true
    }

    inline fun Boolean.ifNot(action: ()->Unit) {
        if (!this) {
            action()
        }
    }
}