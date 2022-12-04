package analyzer.syntax

class LocalPointer(
    private var value: Int
) {
    fun inc() {
        value++
    }

    fun get() = value

    fun set(value: Int) {
        this.value = value
    }
}