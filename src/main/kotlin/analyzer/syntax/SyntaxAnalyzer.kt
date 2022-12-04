package analyzer.syntax

import analyzer.SyntaxResult
import dictionary.Dictionary
import exceptions.SyntaxException
import logger.Logger
import model.BoolValue
import model.Grammar
import model.Linear
import model.Types
import analyzer.syntax.Utils.ifNot
import analyzer.syntax.Utils.or

class SyntaxAnalyzer(
    private val logger: Logger,
    private val provider: SyntaxFileProvider
) {

    private val newIdentifiers = mutableListOf<Int>()
    private val usedIdentifiers = mutableListOf<Int>()
    private val identifierTypes = mutableMapOf<Int, String>()
    private val setTypes = mutableMapOf<Int, MutableList<String>>()

    private var line = 1

    fun run(): SyntaxResult {
        if (isMayBeProgram()) {
            logger.info(Dictionary.syntaxEnd())
            logger.debug("NewIdentifiers : $newIdentifiers\n")
            logger.debug("UsedIdentifiers: $usedIdentifiers\n")
            logger.debug("identifierTypes: $identifierTypes\n")
            logger.debug("setTypes       : $setTypes\n")
        } else {
            throw SyntaxException(line)
        }
        return SyntaxResult(newIdentifiers, usedIdentifiers, identifierTypes, setTypes)
    }

    private fun isMayBeProgram(): Boolean {
        val pointer = LocalPointer(0)
        while (true) {
            Linear
                .or { isMayBeSummary(pointer) }
                .or { isMayBeOperator(pointer) }
                .get().ifNot {
                    return false
                }

            pointer.inc()
            var grammar = provider.getGrammar(pointer.get())
            if (grammar.or(Utils.getLineEnds(), provider, Grammar::isSign).not()) return false
            line++

            pointer.inc()
            grammar = provider.getGrammar(pointer.get())

            if (grammar.isWord("end", provider)) {
                logger.debug("program pattern success\n")
                return true
            }
        }
    }

    private fun isMayBeSummary(pointer: LocalPointer): Boolean {
        val i = LocalPointer(pointer.get())

        if (i.currentIsSign("\n")) {
            // уменьшение на 1 так как строка пустая, и программа уже смотрит на перенос строки, чего быть не должно
            pointer.set(i.get() - 1)
        } else {
            val ids = mutableListOf<Int>()
            while (true) {
                if (i.currentIsIdentifier().not()) return false
                ids.add(i.getGrammarPosition())

                if (i.nextIsSign(",").not()) break

                i.inc()
            }
            if (i.currentIsSign(":").not()) return false

            i.inc()
            val type = isMayBeTypeWord(i)
            if (type.boolean().not()) return false

            if (i.nextIsSign(";").not()) return false

            pointer.set(i.get())
            ids.forEach {
                newIdentifiers.add(it)
                usedIdentifiers.add(it)
                identifierTypes[it] = Utils.getTypesWords()[type.value!!]
            }
        }

        logger.debug("summary pattern success\n")
        return true
    }

    private fun isMayBeTypeWord(pointer: LocalPointer): BoolValue<Int> {
        val i = LocalPointer(pointer.get())

        val type = i.currentIsWords(Utils.getTypesWords())
        if (type.boolean().not()) return BoolValue.theFalse()

        logger.debug("type pattern success\n")
        pointer.set(i.get())

        return type
    }

    private fun isMayBeOtnoshenieSign(pointer: LocalPointer): Boolean {
        val i = LocalPointer(pointer.get())
        val grammar = provider.getGrammar(i.get())
        if (grammar.or(Utils.getOtnoshenieSigns(), provider, Grammar::isSign).not()) return false

        logger.debug("otnoshenie pattern success\n")
        pointer.set(i.get())
        return true
    }

    private fun isMayBeSumSign(pointer: LocalPointer): Boolean {
        val i = LocalPointer(pointer.get())
        val grammar = provider.getGrammar(i.get())
        if (grammar.or(Utils.getSumSigns(), provider, Grammar::isSign).not()) return false

        logger.debug("sum pattern success\n")
        pointer.set(i.get())
        return true
    }

    private fun isMayBeMultiplySign(pointer: LocalPointer): Boolean {
        val i = LocalPointer(pointer.get())
        val grammar = provider.getGrammar(i.get())
        if (grammar.or(Utils.getMultiplySigns(), provider, Grammar::isSign).not()) return false

        logger.debug("multiply pattern success\n")
        pointer.set(i.get())
        return true
    }

    private fun isMayBeLogicWord(pointer: LocalPointer): Boolean {
        val i = LocalPointer(pointer.get())

        if (i.currentIsWords(Utils.getLogicWords()).boolean().not()) return false

        logger.debug("multiply pattern success\n")
        pointer.set(i.get())
        return true
    }

    private fun isMayBeOperator(pointer: LocalPointer): Boolean {
        val result = Linear
            .or { isMayBeSostavnoy(pointer) }
            .or { isMayBePrisvaivaniya(pointer) }
            .or { isMayBeYslovniy(pointer) }
            .or { isMayBeFixCycle(pointer) }
            .or { isMayBeYslCycle(pointer) }
            .or { isMayBeInput(pointer) }
            .or { isMayBeOutput(pointer) }
            .get()
        if (result) {
            logger.debug("operator pattern success\n")
        }
        return result
    }

    private fun isMayBeSostavnoy(pointer: LocalPointer): Boolean {
        val i = LocalPointer(pointer.get())

        if (i.currentIsSign("{").not()) return false

        while (true) {
            i.inc()
            if (isMayBeOperator(i).not()) return false

            if (i.nextIsSign(";").not()) break
        }

        if (i.currentIsSign("}").not()) return false

        logger.debug("Составной оператор pattern success\n")
        pointer.set(i.get())
        return true
    }

    private fun isMayBePrisvaivaniya(pointer: LocalPointer): Boolean {
        var isNew = false
        val identifier: Int
        val i = LocalPointer(pointer.get())

        if (i.currentIsWord("let")) {
            isNew = true
            i.inc()
        }

        if (i.currentIsIdentifier().not()) return false
        identifier = i.getGrammarPosition()

        if (i.nextIsSign("=").not()) return false

        i.inc()
        val t = isMayBeVirazhenie(i)
        if (t.boolean().not()) return false

        logger.debug("Оператор присваивания pattern success\n")
        pointer.set(i.get())
        if (isNew) {
            newIdentifiers.add(identifier)
            identifierTypes[identifier] = t.value!!.type
        } else {
            newIdentifiers.add(-1)
        }
        usedIdentifiers.add(identifier)
        val list = setTypes[identifier] ?: mutableListOf<String>().apply { setTypes[identifier] = this }
        list.add(t.value!!.type)
        return true
    }

    private fun isMayBeVirazhenie(pointer: LocalPointer): BoolValue<Types> {
        val i = LocalPointer(pointer.get())
        val types = mutableListOf<Types>()

        while (true) {
            val t = isMayBeOperand(i)
            if (t.boolean().not()) return BoolValue.theFalse()
            types.add(t.value!!)

            i.inc()
            if (isMayBeOtnoshenieSign(i).not()) break

            i.inc()
        }

        logger.debug("Выражение pattern success\n")
        pointer.set(i.get() - 1)
        return BoolValue(types.combine())
    }

    private fun isMayBeOperand(pointer: LocalPointer): BoolValue<Types> {
        val i = LocalPointer(pointer.get())
        val types = mutableListOf<Types>()

        while (true) {
            val t = isMaySlagaemoe(i)
            if (t.boolean().not()) return BoolValue.theFalse()
            types.add(t.value!!)

            i.inc()
            if (isMayBeSumSign(i).not()) break

            i.inc()
        }

        logger.debug("Операнд pattern success\n")
        pointer.set(i.get() - 1)
        return BoolValue(types.combine())
    }

    private fun isMaySlagaemoe(pointer: LocalPointer): BoolValue<Types> {
        val i = LocalPointer(pointer.get())
        val types = mutableListOf<Types>()

        while (true) {
            val t = isMayBeMultiplayer(i)
            if (t.boolean().not()) return BoolValue.theFalse()
            types.add(t.value!!)

            i.inc()
            if (isMayBeMultiplySign(i).not()) break

            i.inc()
        }

        logger.debug("Слагаемое pattern success\n")
        pointer.set(i.get() - 1)
        return BoolValue(types.combine())
    }

    private fun isMayBeMultiplayer(pointer: LocalPointer): BoolValue<Types> {
        var identifier: Int? = null
        val i = LocalPointer(pointer.get())
        val grammar = provider.getGrammar(i.get())

        var type: Types = Types.UNKNOWN
        val result = Linear
            .or {
                return@or if (grammar.isIdentifier()) {
                    identifier = grammar.itemPosition
                    type = getIdentifierType(grammar.itemPosition)
                    true
                } else {
                    false
                }
            }
            .or {
                return@or if (grammar.isNumber()) {
                    type = getNumberType(grammar.itemPosition)
                    true
                } else {
                    false
                }
            }
            .or {
                return@or if (isMayBeLogicWord(i)) {
                    type = Types.BOOLEAN
                    true
                } else {
                    false
                }
            }
            .or {
                val localI = LocalPointer(i.get())
                if (localI.currentIsSign("!").not()) return@or false

                localI.inc()
                val t = isMayBeMultiplayer(localI)
                if (t.boolean()) {
                    i.set(localI.get())
                    type = t.value!!
                    return@or true
                }
                return@or false
            }
            .or {
                val localI = LocalPointer(i.get())
                if (localI.currentIsSign("(").not()) return@or false

                localI.inc()
                val t = isMayBeMultiplayer(localI)
                if (t.boolean().not()) return@or false

                if (localI.nextIsSign(")").not()) return@or false

                i.set(localI.get())
                type = t.value!!
                return@or true
            }.get()

        if (result) {
            pointer.set(i.get())
            logger.debug("Множитель pattern success\n")
            identifier?.let {
                usedIdentifiers.add(it)
                newIdentifiers.add(-1)
            }
            return BoolValue(type)
        }
        return BoolValue.theFalse()
    }

    private fun getNumberType(position: Int): Types {
        val number = provider.getNumber(position)
        return if (number.contains('.')) {
            Types.REAL
        } else {
            Types.INTEGER
        }
    }

    private fun getIdentifierType(position: Int): Types {
        return Types.parse(identifierTypes[position] ?: "")
    }

    private fun isMayBeYslovniy(pointer: LocalPointer): Boolean {
        val i = LocalPointer(pointer.get())

        if (i.currentIsWord("if").not()) return false

        i.inc()
        val t = isMayBeVirazhenie(i)
        if (t.boolean().not()) return false

        if (i.nextIsWord("then").not()) return false

        i.inc()
        if (isMayBeOperator(i).not()) return false

        i.inc()
        if (i.currentIsWord("end_else").not()) {
            if (i.currentIsWord("else").not()) return false

            i.inc()
            if (isMayBeOperator(i).not()) return false

            if (i.nextIsWord("end_else").not()) return false
        }

        logger.debug("Условный pattern success\n")
        pointer.set(i.get())
        return true
    }

    private fun isMayBeFixCycle(pointer: LocalPointer): Boolean {
        val i = LocalPointer(pointer.get())

        if (i.currentIsWord("for").not()) return false

        if (i.nextIsSign("(").not()) return false

        if (i.nextIsSign(";").not()) {
            if (isMayBePrisvaivaniya(i).not()) return false

            if (i.nextIsSign(";").not()) return false
        }

        if (i.nextIsSign(";").not()) {
            val t = isMayBeVirazhenie(i)
            if (t.boolean().not()) return false

            if (i.nextIsSign(";").not()) return false
        }

        if (i.nextIsSign(")").not()) {
            if (isMayBeOperator(i).not()) return false

            if (i.nextIsSign(")").not()) return false
        }

        i.inc()
        if (isMayBeOperator(i).not()) return false

        logger.debug("Фикс. цикл pattern success\n")
        pointer.set(i.get())
        return true
    }

    private fun isMayBeYslCycle(pointer: LocalPointer): Boolean {
        val i = LocalPointer(pointer.get())

        if (i.currentIsWord("do").not()) return false

        if (i.nextIsWord("while").not()) return false

        i.inc()
        val t = isMayBeVirazhenie(i)
        if (t.boolean().not()) return false

        i.inc()
        if (isMayBeOperator(i).not()) return false

        if (i.nextIsWord("loop").not()) return false

        logger.debug("Условный цикл pattern success\n")
        pointer.set(i.get())
        return true
    }

    private fun isMayBeInput(pointer: LocalPointer): Boolean {
        val ids = mutableListOf<Int>()
        val i = LocalPointer(pointer.get())

        if (i.currentIsWord("input").not()) return false

        if (i.nextIsSign("(").not()) return false

        i.inc()
        while (true) {
            if (i.currentIsIdentifier().not()) return false
            ids.add(i.getGrammarPosition())

            if (i.nextIsSign(")")) break
        }

        logger.debug("Оператор ввода pattern success\n")
        pointer.set(i.get())
        ids.forEach {
            usedIdentifiers.add(it)
            newIdentifiers.add(-1)
        }
        return true
    }

    private fun isMayBeOutput(pointer: LocalPointer): Boolean {
        val i = LocalPointer(pointer.get())

        if (i.currentIsWord("output").not()) return false

        if (i.nextIsSign("(").not()) return false

        i.inc()
        while (true) {
            val t = isMayBeVirazhenie(i)
            if (t.boolean().not()) return false

            if (i.nextIsSign(")")) break
        }

        logger.debug("Оператор вывода pattern success\n")
        pointer.set(i.get())
        return true
    }

    private fun LocalPointer.currentIsWord(word: String): Boolean {
        val grammar = provider.getGrammar(this.get())
        return grammar.isWord(word, provider)
    }

    private fun LocalPointer.currentIsWords(words: List<String>): BoolValue<Int> {
        val grammar = provider.getGrammar(this.get())
        for (word in words.withIndex()) {
            if (grammar.isWord(word.value, provider)) {
                return BoolValue(word.index)
            }
        }
        return BoolValue.theFalse()
    }

    private fun LocalPointer.nextIsWord(word: String): Boolean {
        this.inc()
        return currentIsWord(word)
    }

    private fun LocalPointer.currentIsSign(sign: String): Boolean {
        val grammar = provider.getGrammar(this.get())
        return grammar.isSign(sign, provider)
    }

    private fun LocalPointer.nextIsSign(sign: String): Boolean {
        this.inc()
        return currentIsSign(sign)
    }

    private fun LocalPointer.currentIsIdentifier(): Boolean {
        val grammar = provider.getGrammar(this.get())
        return grammar.isIdentifier()
    }

    private fun LocalPointer.getGrammarPosition(): Int {
        return provider.getGrammar(this.get()).itemPosition
    }

    private fun List<Types>.combine(): Types {
        val first = get(0)
        return if (all { it == first }) {
            first
        } else {
            Types.UNKNOWN
        }
    }
}