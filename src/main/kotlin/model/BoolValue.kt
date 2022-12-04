package model

class BoolValue<T>(
    val value: T?
) {

    fun boolean(): Boolean {
        return value != null
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return value == null
        if (other is Boolean) {
            return boolean() == other
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    companion object {
        fun <T> theFalse() = BoolValue<T>(null)
    }
}