package analyzer.syntax

import model.Grammar

interface SyntaxFileProvider {

    fun getGrammar(index: Int): Grammar

    fun getSign(position: Int): String
    fun getWord(position: Int): String
    fun getNumber(position: Int): String
}