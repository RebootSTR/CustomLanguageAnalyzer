package analyzer.lexical

class Stack {

    private val builder = StringBuilder()

    fun put(char: Char) {
        builder.append(char)
    }

    fun clear() {
        builder.clear()
    }

    fun get(): String {
        return builder.toString()
    }

    fun isEmpty(): Boolean {
        return builder.isEmpty()
    }

    fun getLast(): Char {
        return builder.last()
    }
}