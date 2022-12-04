package analyzer.lexical

object NumberParser {

    fun parseNumberToCC2(number: String, type: NumberType): String {
        return when (type) {
            NumberType.CC_2 -> parseCC_2(number)
            NumberType.CC_8 -> parseCC_8(number)
            NumberType.CC_10 -> parseCC_10(number)
            NumberType.CC_16 -> parseCC_16(number)
            NumberType.CC_10_D -> parseCC_10_D(number)
            NumberType.CC_10_E -> parseCC_10_E(number)
            NumberType.CC_10_E_WITH_SIGN -> parseCC_10_E(number)
        }
    }

    private fun parseCC_2(number: String): String {
        return number.replace("b", "")
    }

    private fun parseCC_8(number: String): String {
        return number.replace("o", "").toInt(8).toString(2)
    }

    private fun parseCC_10(number: String): String {
        return number.replace("d", "").toInt().toString(2)
    }

    private fun parseCC_16(number: String): String {
        return number.replace("h", "").toInt(16).toString(2)
    }

    private fun parseCC_10_D(number: String): String {
        return number.replace("d", "")
            .split('.')
            .joinToString(separator = ".") { it.toInt().toString(2) }
    }

    private fun parseCC_10_E(number: String): String {
        val array = number.split('e')
        var num = array[0].toDouble()
        val e = array[1].toInt()
        val k: Double = if (array[1].contains('-')) {
            1/10.toDouble()
        } else {
            10.toDouble()
        }
        for (i in 0 until e) {
           num *= k
        }
        return parseCC_10_D(num.toString())
    }
}

enum class NumberType {
    CC_2,
    CC_8,
    CC_10,
    CC_16,
    CC_10_D,
    CC_10_E,
    CC_10_E_WITH_SIGN,
}