package model

import analyzer.syntax.SyntaxFileProvider

class Grammar(
    val table: Tables,
    val itemPosition: Int
) {
    fun isSign() = table == Tables.SIGN
    fun isSign(sign: String, provider: SyntaxFileProvider): Boolean {
        if (isSign().not()) return false
        return provider.getSign(itemPosition) == sign
    }

    fun isWord() = table == Tables.WORD
    fun isWord(word: String, provider: SyntaxFileProvider): Boolean {
        if (isWord().not()) return false
        return provider.getWord(itemPosition) == word
    }

    fun isIdentifier() = table == Tables.IDENTIFIER
    fun isNumber() = table == Tables.NUMBER

    override fun toString(): String {
        return "(${table.ordinal},$itemPosition)"
    }
}