package model

enum class Types(val type: String) {
    INTEGER("integer"),
    REAL("real"),
    BOOLEAN("boolean"),
    UNKNOWN("");

    companion object {
        fun parse(value: String) = values().firstOrNull { it.type == value } ?: UNKNOWN
    }
}