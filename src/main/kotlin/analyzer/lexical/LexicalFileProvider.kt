package analyzer.lexical

import model.Grammar

interface LexicalFileProvider {

    fun getNextSymbol(): Char

    fun findWord(string: String): Int

    fun canBeSign(char: Char): Boolean

    fun findSign(string: String): Int

    fun saveIdentifier(identifier: String): Int

    fun saveIdentifierFile()

    fun saveNumber(number: String, type: NumberType): Int

    fun saveNumbersFile()

    fun saveLexicalResult(grammars: List<Grammar>)
}