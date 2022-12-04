package exceptions

open class LexicalException: Exception {

    constructor(): super()
    constructor(message: String): super(message)
}