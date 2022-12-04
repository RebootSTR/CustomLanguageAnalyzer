package logger

/**
 * Класс вывода в консоль
 */
class Console(
    level: Level
): Logger(
    level
) {

    override fun print(message: String) {
        if (message.last() == '\n') {
            kotlin.io.print(message)
        } else {
            kotlin.io.print("$message ")
        }
    }

    override fun printResult(message: String) {
        print(message)
    }
}