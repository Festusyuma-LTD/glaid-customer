package festusyuma.com.glaid.helpers

import festusyuma.com.glaid.COUNTRY_CODE

fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.first().toUpperCase() + it.substring(1) }

fun String.getFirst(): String = split(" ")[0]

fun String.addCountryCode(): String {
    return if (this.length >= 11 && this[0] == '0') {
        if (this[0] == '0') {
            COUNTRY_CODE + this.substring(1)
        }else COUNTRY_CODE + this
    }else this
}