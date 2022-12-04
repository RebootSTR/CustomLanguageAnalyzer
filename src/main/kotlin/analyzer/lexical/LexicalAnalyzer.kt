package analyzer.lexical

import dictionary.Dictionary
import analyzer.lexical.exceptions.LexicalInternalException
import analyzer.lexical.exceptions.CharNotAllowedException
import analyzer.lexical.exceptions.SignFormatException
import logger.Logger
import model.Grammar
import model.Tables
import java.io.EOFException

class LexicalAnalyzer(
    private val logger: Logger,
    private val provider: LexicalFileProvider
) {

    companion object {
        private const val EOF = 65535
    }

    private val stack = Stack()

    private val grammars = mutableListOf<Grammar>()

    fun run() {
        logger.info(Dictionary.startMessage())
        analyze()
        provider.saveIdentifierFile()
        provider.saveNumbersFile()
        provider.saveLexicalResult(grammars)
    }

    private fun analyze() {
        try {
            while (true) {
                val char = if (stack.isEmpty().not()) {
                    stack.getLast()
                } else {
                    provider.getNextSymbol()
                }
                stateH(char)
            }
        } catch (ex: EOFException) {
            logger.info(Dictionary.lexicalEndMessage())
        }
    }

    private fun saveToResult(result: Grammar) {
        grammars.add(result)
    }

    private fun stateH(char: Char) {
        stack.clear()
        when {
            char.isLetter() -> {
                stack.put(char)
                stateH1()
            }

            char == '%' -> {
                stateC()
            }

            char.isSpace() -> return

            char.isDigit01() -> {
                stack.put(char)
                state2cc()
            }

            char.isDigit234567() -> {
                stack.put(char)
                state8cc()
            }

            char.isDigit89() -> {
                stack.put(char)
                state10cc()
            }

            char == '.' -> {
                stack.put(char)
                stateD()
            }

            char == '!' -> {
                stack.put(char)
                stateSingleOrDoubleSign('=')
            }

            char == '<' -> {
                stack.put(char)
                stateSingleOrDoubleSign('=')
            }

            char == '>' -> {
                stack.put(char)
                stateSingleOrDoubleSign('=')
            }

            char == '|' -> {
                stack.put(char)
                stateDoubleSign('|')
            }

            char == '&' -> {
                stack.put(char)
                stateDoubleSign('&')
            }

            char == '=' -> {
                stack.put(char)
                stateSingleOrDoubleSign('=')
            }

            char.code == EOF -> throw EOFException()
            else -> {
                stack.put(char)
                stateSingleState()
            }
        }
    }

    private fun stateSingleState() {
        val index = provider.findSign(stack.get())
        if (index == -1) throw CharNotAllowedException("Символ '${stack.get()}' не является символом языка")
        saveToResult(Grammar(Tables.SIGN, index))
        stack.clear()
    }

    private fun stateSingleOrDoubleSign(endChar: Char) {
        val char = provider.getNextSymbol()
        when {
            char == endChar -> {
                stack.put(char)
                val index = provider.findSign(stack.get())
                if (index == -1) throw LexicalInternalException("Sign analyze error. Stack = ${stack.get()}")
                saveToResult(Grammar(Tables.SIGN, index))
                stack.clear()
            }

            char.isNotLimiter(provider) -> {
                val index = provider.findSign(stack.get())
                if (index == -1) throw LexicalInternalException("Sign analyze error. Stack = ${stack.get()}")
                saveToResult(Grammar(Tables.SIGN, index))
                stack.put(char)
            }

            else -> {
                throw SignFormatException("'${stack.get()}' + '$char' не является разделителем")
            }
        }
    }

    private fun stateDoubleSign(endChar: Char) {
        val char = provider.getNextSymbol()
        if (char == endChar) {
            stack.put(char)
            val index = provider.findSign(stack.get())
            if (index == -1) throw LexicalInternalException("Sign analyze error. Stack = ${stack.get()}")
            saveToResult(Grammar(Tables.SIGN, index))
            stack.clear()
        } else {
            throw NumberFormatException("'${stack.get()}' + '$char' не является разделителем")
        }
    }


    private fun stateH1() {
        logger.debug("накопление букв для KEYWORD\n")
        val char = saveStackWhile { it.isDigit() || it.isLetter() || it == '_' }

        logger.debug("проверка TW\n")
        val index = provider.findWord(stack.get())
        if (index != -1) {
            saveToResult(Grammar(Tables.WORD, index))
        } else {
            val id = provider.saveIdentifier(stack.get())
            saveToResult(Grammar(Tables.IDENTIFIER, id))
        }
        stack.put(char)
    }

    private fun stateC() {
        logger.debug("начало комментария\n")
        saveStackWhile { it != '%' }
        stack.clear()
        logger.debug("конец комментария\n")
    }

    private fun state2cc() {
        logger.debug("накопление двоичного числа\n")
        val char = saveStackWhile { it.isDigit01() }
        when {
            char.isDigit234567() -> {
                logger.debug("Переквалификация (8cc)\n")
                stack.put(char)
                state8cc()
            }

            char.isDigit89() -> {
                logger.debug("Переквалификация (10сс)\n")
                stack.put(char)
                state10cc()
            }

            char == '.' -> {
                logger.debug("Переквалификация (D)\n")
                stack.put(char)
                stateD()
            }

            char.isLetterACF() -> {
                logger.debug("Переквалификация (16cc)\n")
                stack.put(char)
                state16cc()
            }

            char.isLetterB() -> {
                stack.put(char)
                state2cc1()
            }

            char.isLetterD() -> {
                logger.debug("Переквалификация (10сс')\n")
                stack.put(char)
                state10cc1()
            }

            char.isLetterE() -> {
                logger.debug("Переквалификация (E)\n")
                stack.put(char)
                stateE()
            }

            char.isLetterH() -> {
                logger.debug("Переквалификация (16cc')\n")
                stack.put(char)
                state16cc1()
            }

            char.isLetterO() -> {
                logger.debug("Переквалификация (8сс')\n")
                stack.put(char)
                state8cc1()
            }

            char.canBeSign(provider) -> {
                saveNumberFromStack(NumberType.CC_2)
                stack.put(char)
            }

            else -> {
                throw NumberFormatException("'${stack.get()}' + '$char' не является числом")
            }
        }
    }

    private fun state2cc1() {
        val char = provider.getNextSymbol()
        when {
            char.is16Digit() -> {
                logger.debug("Переквалификация (16cc)\n")
                stack.put(char)
                state16cc()
            }

            char.isLetterH() -> {
                logger.debug("Переквалификация (16cc')\n")
                stack.put(char)
                state16cc1()
            }

            char.canBeSign(provider) -> {
                saveNumberFromStack(NumberType.CC_2)
                stack.put(char)
            }

            else -> {
                throw NumberFormatException("'${stack.get()}' + '$char' не является числом")
            }
        }
    }

    private fun state8cc() {
        logger.debug("накопление восьмиричного числа\n")
        val char = saveStackWhile { it.isDigit01234567() }
        when {
            char.isDigit89() -> {
                logger.debug("Переквалификация (10сс)\n")
                stack.put(char)
                state10cc()
            }

            char == '.' -> {
                logger.debug("Переквалификация (D)\n")
                stack.put(char)
                stateD()
            }

            char.isLetterABCF() -> {
                logger.debug("Переквалификация (16cc)\n")
                stack.put(char)
                state16cc()
            }

            char.isLetterD() -> {
                logger.debug("Переквалификация (10сс')\n")
                stack.put(char)
                state10cc1()
            }

            char.isLetterE() -> {
                logger.debug("Переквалификация (E)\n")
                stack.put(char)
                stateE()
            }

            char.isLetterH() -> {
                logger.debug("Переквалификация (16cc')\n")
                stack.put(char)
                state16cc1()
            }

            char.isLetterO() -> {
                stack.put(char)
                state8cc1()
            }

            char.canBeSign(provider) -> {
                saveNumberFromStack(NumberType.CC_8)
                stack.put(char)
            }

            else -> {
                throw NumberFormatException("'${stack.get()}' + '$char' не является числом")
            }
        }
    }

    private fun state8cc1() {
        val char = provider.getNextSymbol()
        when {
            char.canBeSign(provider) -> {
                saveNumberFromStack(NumberType.CC_8)
                stack.put(char)
            }

            else -> {
                throw NumberFormatException("'${stack.get()}' + '$char' не является числом")
            }
        }
    }

    private fun state10cc() {
        logger.debug("накопление десятичного числа\n")
        val char = saveStackWhile { it.isDigit() }
        when {
            char.isLetterH() -> {
                logger.debug("Переквалификация (16cc')\n")
                stack.put(char)
                state16cc1()
            }

            char == '.' -> {
                logger.debug("Переквалификация (D)\n")
                stack.put(char)
                stateD()
            }

            char.isLetterABCF() -> {
                logger.debug("Переквалификация (16cc)\n")
                stack.put(char)
                state16cc()
            }

            char.isLetterD() -> {
                stack.put(char)
                state10cc1()
            }

            char.isLetterE() -> {
                logger.debug("Переквалификация (E)\n")
                stack.put(char)
                stateE()
            }

            char.canBeSign(provider) -> {
                saveNumberFromStack(NumberType.CC_10)
                stack.put(char)
            }

            else -> {
                throw NumberFormatException("'${stack.get()}' + '$char' не является числом")
            }
        }
    }

    private fun state10cc1() {
        val char = provider.getNextSymbol()
        when {
            char.is16Digit() -> {
                logger.debug("Переквалификация (16cc)\n")
                stack.put(char)
                state16cc()
            }

            char.isLetterH() -> {
                logger.debug("Переквалификация (16cc')\n")
                stack.put(char)
                state16cc1()
            }

            char.canBeSign(provider) -> {
                saveNumberFromStack(NumberType.CC_10)
                stack.put(char)
            }

            else -> {
                throw NumberFormatException("'${stack.get()}' + '$char' не является числом")
            }
        }
    }

    private fun state16cc() {
        logger.debug("накопление шестнадцатиричного числа\n")
        val char = saveStackWhile { it.is16Digit() }
        when {
            char.isLetterH() -> {
                stack.put(char)
                state16cc1()
            }

            char.canBeSign(provider) -> {
                saveNumberFromStack(NumberType.CC_16)
                stack.put(char)
            }

            else -> {
                throw NumberFormatException("'${stack.get()}' + '$char' не является числом")
            }
        }
    }

    private fun state16cc1() {
        val char = provider.getNextSymbol()
        when {
            char.canBeSign(provider) -> {
                saveNumberFromStack(NumberType.CC_16)
                stack.put(char)
            }

            else -> {
                throw NumberFormatException("'${stack.get()}' + '$char' не является числом")
            }
        }
    }

    private fun stateE() {
        logger.debug("накопление Е числа\n")
        val char = provider.getNextSymbol()
        when {
            char.isPlusMinus() -> {
                stack.put(char)
                stateE1()
            }

            char.isDigit() -> {
                stack.put(char)
                stateE2()
            }

            char.isLetterABCDEF() -> { // not error
                logger.debug("Переквалификация (16cc)\n")
                stack.put(char)
                state16cc()
            }

            char.isLetterH() -> {
                logger.debug("Переквалификация (16cc')\n")
                stack.put(char)
                state16cc1()
            }

            char.canBeSign(provider) -> {
                saveNumberFromStack(NumberType.CC_16)
                stack.put(char)
            }

            else -> {
                throw NumberFormatException("'${stack.get()}' + '$char' не является числом")
            }
        }
    }

    private fun stateE1() {
        val char = provider.getNextSymbol()
        when {
            char.isDigit() -> {
                stack.put(char)
                stateE3()
            }

            else -> {
                throw NumberFormatException("'${stack.get()}' + '$char' не является числом")
            }
        }
    }

    private fun stateE2() {
        logger.debug("накопление хваоста для Е (буквы разрешены)\n")
        val char = saveStackWhile { it.isDigit() }
        when {
            char.isLetterABCDEF() -> {
                logger.debug("Переквалификация (16cc)\n")
                stack.put(char)
                state16cc()
            }

            char.isLetterH() -> {
                logger.debug("Переквалификация (16cc')\n")
                stack.put(char)
                state16cc1()
            }

            char.canBeSign(provider) -> {
                saveNumberFromStack(NumberType.CC_10_E)
                stack.put(char)
            }

            else -> {
                throw NumberFormatException("'${stack.get()}' + '$char' не является числом")
            }
        }
    }

    private fun stateE3() {
        logger.debug("накопление хваоста для Е (буквы запрещены)\n")
        val char = saveStackWhile { it.isDigit() }
        when {
            char.canBeSign(provider) -> {
                saveNumberFromStack(NumberType.CC_10_E_WITH_SIGN)
                stack.put(char)
            }

            else -> {
                throw NumberFormatException("'${stack.get()}' + '$char' не является числом")
            }
        }
    }

    private fun stateD() {
        val char = provider.getNextSymbol()
        when {
            char.isDigit() -> {
                stack.put(char)
                stateD1()
            }

            else -> {
                throw NumberFormatException("'${stack.get()}' + '$char' не является числом")
            }
        }
    }

    private fun stateD1() {
        logger.debug("накопление хваоста для D\n")
        val char = saveStackWhile { it.isDigit() }
        when {
            char.isLetterE() -> {
                logger.debug("Переквалификация (ED)\n")
                stack.put(char)
                stateED()
            }

            char.canBeSign(provider) -> {
                saveNumberFromStack(NumberType.CC_10_D)
                stack.put(char)
            }

            else -> {
                throw NumberFormatException("'${stack.get()}' + '$char' не является числом")
            }
        }
    }

    private fun stateED() {
        val char = provider.getNextSymbol()
        when {
            char.isPlusMinus() -> {
                stack.put(char)
                stateE1()
            }

            char.isDigit() -> {
                stack.put(char)
                stateE3()
            }

            else -> {
                throw NumberFormatException("'${stack.get()}' + '$char' не является числом")
            }
        }
    }

    private fun saveNumberFromStack(type: NumberType) {
        val index = provider.saveNumber(stack.get(), type)
        saveToResult(Grammar(Tables.NUMBER, index))
    }

    private fun saveStackWhile(predicate: (Char) -> Boolean): Char {
        var char: Char
        while (true) {
            char = provider.getNextSymbol()
            if (predicate(char)) {
                stack.put(char)
            } else {
                return char
            }
        }
    }
}