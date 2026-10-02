package es.unizar.urlshortener.links.adapters.persistence

/**
 * Unpadded base62. [START] is the first id whose encoding has [MIN_CODE_LENGTH] characters.
 */
object Base62 {
    const val MIN_CODE_LENGTH = 4
    const val RADIX = 62
    val START: Long = pow(MIN_CODE_LENGTH - 1)

    private const val ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

    fun encode(id: Long): String {
        var n = id
        val out = StringBuilder()
        do {
            out.append(ALPHABET[(n % RADIX).toInt()])
            n /= RADIX
        } while (n > 0)
        return out.reverse().toString()
    }

    private fun pow(exponent: Int): Long {
        var value = 1L
        repeat(exponent) { value *= RADIX }
        return value
    }
}
