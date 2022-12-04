package dictionary

object Dictionary {

    private const val ERROR_TEMPLATE: String = "[ОШИБКА]"
    private const val FILL_FILE_MESSAGE: String = "Файл %s создан. Заполните его, пожалуйста.\n"
    private const val FILL_SORTED_MESSAGE: String = "Файл %s был отсортирован для использования бинарного поиска\n"
    private const val START_MESSAGE: String = "Файлы инициализированы, начало анализа\n"
    private const val LEXICAL_END_MESSAGE: String = "\nЛексический анализ завершен\n"
    private const val SYNTAX_END_MESSAGE = "Синтаксический анализ завершен\n"
    private const val SEMANTIC_END_MESSAGE = "Семантический анализ завершен\n"
    private const val LEXICAL_COMPLETE_MESSAGE = "Результат лексического анализа сохранен в файл %s\n"
    private const val INTERNAL_ERROR = "\n${ERROR_TEMPLATE}Внутренняя ошибка анализатора: %s\n"
    private const val ERROR = "\nОшибка в программе: %s\n"
    private const val LEXICAL_ERROR = "\nЛексический анализ завершен с ошибкой!\n"
    private const val SEMANTIC_ERROR = "Семантический анализ завершен с ошибкой!\n"
    private const val SYNTAX_ERROR = "Синтаксический анализ завершен с ошибкой (строка %d)!\n"
    private const val MULTI_INIT_ERROR = "${ERROR_TEMPLATE}Обнаружено множественное объявление переменных: %s\n"
    private const val VARIABLE_NOT_INIT_ERROR = "${ERROR_TEMPLATE}Обнаружена не инициализированная переменная: %s\n"
    private const val VARIABLE_NOT_INIT_YET_ERROR = "${ERROR_TEMPLATE}Обнаружена поздно инициализированная переменная: %s\n"
    private const val VARIABLE_TYPE_ERROR = "${ERROR_TEMPLATE}Обнаружено использование несовместимых типов\n"
    private const val DIVIDER = "   ----\n"

    private const val AUTHOR_COPYRIGHT = "" +
            "|-----------------------------------|\n" +
            "|  Анализатор был создан для ТАиФЯ  |\n" +
            "|  Автор: Рафиков Айдар Робертович  |\n" +
            "|        Год создания: 2022г        |\n" +
            "|-----------------------------------|\n" +
            "\n"

    fun divider(): String {
        return DIVIDER
    }

    fun multiInitError(variables: String): String {
        return MULTI_INIT_ERROR.format(variables)
    }

    fun variableNotInitError(identifier: String): String {
        return VARIABLE_NOT_INIT_ERROR.format(identifier)
    }

    fun variableNotInitYetError(identifier: String): String {
        return VARIABLE_NOT_INIT_YET_ERROR.format(identifier)
    }

    fun variableTypeCompatibilityError(): String {
        return VARIABLE_TYPE_ERROR
    }
    fun startMessage() = START_MESSAGE
    fun lexicalEndMessage() = LEXICAL_END_MESSAGE

    fun lexicalCompleteMessage(filename: String): String {
        return LEXICAL_COMPLETE_MESSAGE.format(filename)
    }

    fun fillFile(filename: String): String {
        return FILL_FILE_MESSAGE.format(filename)
    }

    fun fileSorted(filename: String): String {
        return FILL_SORTED_MESSAGE.format(filename)
    }

    fun author(): String {
        return AUTHOR_COPYRIGHT
    }

    fun lexicalError(): String {
        return LEXICAL_ERROR
    }

    fun semanticError(): String {
        return SEMANTIC_ERROR
    }

    fun syntaxError(line: Int): String {
        return SYNTAX_ERROR.format(line)
    }

    fun syntaxEnd(): String {
        return SYNTAX_END_MESSAGE
    }

    fun semanticEnd(): String {
        return SEMANTIC_END_MESSAGE
    }

    fun internalError(message: String?): String {
        return INTERNAL_ERROR.format(message)
    }

    fun error(message: String?): String {
        return ERROR.format(message)
    }
}