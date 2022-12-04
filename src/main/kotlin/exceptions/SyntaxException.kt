package exceptions

open class SyntaxException: Exception {

    var line = 0

    constructor(): super()
    constructor(message: String): super(message)
    constructor(line: Int): super() {
        this.line = line
    }
}