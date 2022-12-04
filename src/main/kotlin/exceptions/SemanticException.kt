package exceptions

open class SemanticException: Exception {

    constructor(): super()
    constructor(message: String): super(message)
}