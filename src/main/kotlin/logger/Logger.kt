package logger

/**
 * Интерфейс вывода текста
 */
abstract class Logger(
    private val level: Level
) {
    enum class Level {
        INFO,
        DEBUG
    }

    protected abstract fun print(message: String)
    protected abstract fun printResult(message: String)

    fun info(message: String) {
        print(message)
    }

    fun debug(message: String) {
        if (level.ordinal >= Level.DEBUG.ordinal) {
            print("DEBUG::${message}")
        }
    }
}