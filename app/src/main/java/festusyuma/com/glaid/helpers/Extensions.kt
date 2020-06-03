package festusyuma.com.glaid.helpers

import java.util.*

fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.first().toUpperCase() + it.substring(1) }