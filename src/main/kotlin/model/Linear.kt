package model

class Linear(
    private var result: Boolean
) {

    fun or(predicate: () -> Boolean): Linear {
        if (result) return this // пропуск вычеслений
        result = predicate()
        return this
    }

    fun get() = result

    companion object {
        inline fun or(predicate: () -> Boolean): Linear {
            return Linear(predicate())
        }
    }
}