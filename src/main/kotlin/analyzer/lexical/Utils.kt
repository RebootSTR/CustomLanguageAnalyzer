package analyzer.lexical

private const val EOF_CODE = 65535

fun Char.isDigit01(): Boolean {
    return when (this) {
        '0', '1' -> true
        else -> false
    }
}

fun Char.isDigit01234567(): Boolean {
    return when (this) {
        '0', '1', '2', '3', '4', '5', '6', '7' -> true
        else -> false
    }
}

fun Char.isDigit89(): Boolean {
    return when (this) {
        '8', '9' -> true
        else -> false
    }
}

fun Char.isDigit234567(): Boolean {
    return when (this) {
        '2', '3', '4', '5', '6', '7' -> true
        else -> false
    }
}

fun Char.isLetterACF(): Boolean {
    return when (this) {
        'a', 'c', 'f' -> true
        else -> false
    }
}

fun Char.isLetterABCF(): Boolean {
    return when (this) {
        'a', 'b', 'c', 'f' -> true
        else -> false
    }
}

fun Char.isLetterABCDEF(): Boolean {
    return when (this) {
        'a', 'b', 'c', 'd', 'e', 'f' -> true
        else -> false
    }
}

fun Char.isLetterB(): Boolean {
    return when (this) {
        'b' -> true
        else -> false
    }
}

fun Char.isLetterD(): Boolean {
    return when (this) {
        'd' -> true
        else -> false
    }
}

fun Char.isLetterE(): Boolean {
    return when (this) {
        'e' -> true
        else -> false
    }
}

fun Char.isLetterH(): Boolean {
    return when (this) {
        'h' -> true
        else -> false
    }
}

fun Char.isLetterO(): Boolean {
    return when (this) {
        'o' -> true
        else -> false
    }
}

fun Char.isSpace(): Boolean {
    return when (this) {
        ' ', '\t', '\r' -> true
        else -> false
    }
}

fun Char.isNotLimiter(provider: LexicalFileProvider): Boolean {
    return provider.canBeSign(this).not()
}

fun Char.canBeSign(provider: LexicalFileProvider): Boolean {
    return isSpace() || provider.canBeSign(this) || this == Char(EOF_CODE)
}

fun Char.is16Digit(): Boolean {
    return isDigit() || when (this) {
        'a', 'b', 'c', 'd', 'e', 'f' -> true
        else -> false
    }
}

fun Char.isPlusMinus(): Boolean {
    return when (this) {
        '+', '-' -> true
        else -> false
    }
}

