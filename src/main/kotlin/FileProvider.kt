import dictionary.Dictionary
import analyzer.lexical.LexicalFileProvider
import analyzer.lexical.NumberParser
import analyzer.lexical.NumberType
import analyzer.lexical.exceptions.FilesNotInitializedException
import analyzer.semantic.SemanticFileProvider
import logger.Logger
import model.Grammar
import analyzer.syntax.SyntaxFileProvider
import analyzer.syntax.exceptions.SyntaxInternalException
import java.io.File
import java.io.InputStreamReader
import java.util.*

class FileProvider(
    private val logger: Logger
): LexicalFileProvider, SyntaxFileProvider, SemanticFileProvider {

    companion object {
        private const val WORDS_FILE = "words.txt"
        private const val SIGN_FILE = "signs.txt"
        private const val CODE_FILE = "program.txt"
        private const val IDENTIFIERS_FILE = "output/identifiers.txt"
        private const val NUMBERS_FILE = "output/numbers.txt"
        private const val LEXICAL_RESULT_FILENAME = "output/lexical_result.txt"
    }

    private val words = File(WORDS_FILE)
    private val sign = File(SIGN_FILE)
    private val code = File(CODE_FILE)

    private var codeReader: InputStreamReader? = null

    private val wordsList = mutableListOf<String>()
    private val signList = mutableListOf<String>()

    private val identifiers = mutableListOf<String>()
    private val numbers = mutableListOf<String>()

    private val grammars = mutableListOf<Grammar>()

    fun initFiles() {
        var result = true
        result = initFileAndSort(words, wordsList) && result
        result = initFileAndSort(sign, signList) && result
        result = initFile(code) && result
        if (!result) {
            throw FilesNotInitializedException()
        }

        codeReader = code.reader()
    }

    private fun initFileAndSort(file: File, putTo: MutableList<String>): Boolean {
        val result = initFile(file)
        if (result) {
            sortFile(file, putTo)
        }
        return result
    }

    private fun initFile(file: File): Boolean {
        if (file.exists().not()) {
            file.createNewFile()
            logger.info(Dictionary.fillFile(file.name))
            return false
        }
        return true
    }

    private fun sortFile(file: File, putTo: MutableList<String>) {
        val lines = file.readLines().map {
            it.replace("\\n", "\n")
        }
        val sorted = lines.sorted()
        if (lines != sorted) {
            file.writeText(sorted.joinToString(separator = "\n") {
                it.replace("\n", "\\n")
            })
            logger.info(Dictionary.fileSorted(file.name))
        }
        putTo.addAll(sorted)
    }

    override fun getNextSymbol(): Char {
        val sym = codeReader!!.read()
        return sym.toChar().lowercaseChar()
    }

    override fun findWord(string: String): Int {
        return wordsList.binarySearch(string)
    }

    override fun canBeSign(char: Char): Boolean {
        return signList.map { it[0] }.binarySearch(char) >= 0
    }

    override fun findSign(string: String): Int {
        return signList.binarySearch(string)
    }

    private fun List<Char>.binarySearch(key: Char): Int {
        val index = Collections.binarySearch(this, key)
        return if (index >= 0) index else -1
    }

    private fun List<String>.binarySearch(key: String): Int {
        val index = Collections.binarySearch(this, key)
        return if (index >= 0) index else -1
    }

    override fun saveIdentifier(identifier: String) = save(identifier, identifiers)
    override fun saveIdentifierFile() = saveFile(identifiers, IDENTIFIERS_FILE)

    override fun saveNumber(number: String, type: NumberType): Int {
        val cc2 = NumberParser.parseNumberToCC2(number, type)
        return save(cc2, numbers)
    }

    override fun saveNumbersFile() = saveFile(numbers, NUMBERS_FILE)

    override fun saveLexicalResult(grammars: List<Grammar>) {
        val file = File(LEXICAL_RESULT_FILENAME)
        file.parentFile.mkdirs()
        file.createNewFile()
        file.writeText(grammars.joinToString(separator = " "))
        this.grammars.addAll(grammars)
        logger.info(Dictionary.lexicalCompleteMessage(LEXICAL_RESULT_FILENAME))
    }

    private fun save(what: String, to: MutableList<String>): Int {
        val index = to.indexOf(what)
        if (index == -1) {
            to.add(what)
            return to.size - 1
        }
        return index
    }

    private fun saveFile(from: MutableList<String>, filename: String) {
        val file = File(filename)
        file.parentFile.mkdirs()
        file.createNewFile()
        file.writeText(from.joinToString(separator = "\n"))
    }

    override fun getGrammar(index: Int): Grammar {
        try {
            return grammars[index]
        } catch (ex: ArrayIndexOutOfBoundsException) {
            throw SyntaxInternalException(ex.message ?: "")
        }
    }

    override fun getSign(position: Int): String {
        return signList[position]
    }

    override fun getWord(position: Int): String {
        return wordsList[position]
    }

    override fun getNumber(position: Int): String {
        return numbers[position]
    }

    override fun getIdentifier(position: Int): String {
        return identifiers[position]
    }
}