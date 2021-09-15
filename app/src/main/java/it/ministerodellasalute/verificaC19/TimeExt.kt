package it.ministerodellasalute.verificaC19

import java.text.SimpleDateFormat
import java.util.*

const val YEAR_MONTH_DAY = "yyyy-MM-dd"
const val DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
const val FORMATTED_YEAR_MONTH_DAY = "MMM d, yyyy"
const val FORMATTED_DATE_TIME = "MMM d, yyyy, HH:mm"
const val FORMATTED_BIRTHDAY_DATE = "dd/MM/yyyy"
const val FORMATTED_DATE_LAST_SYNC = "dd/MM/yyyy, HH:mm"
const val FORMATTED_VALIDATION_DATE = "HH:mm, dd/MM/yyyy"

fun String.parseFromTo(from: String, to: String): String {
    return try {
        val parser = SimpleDateFormat(from, Locale.getDefault())
        val formatter = SimpleDateFormat(to, Locale.getDefault())
        return formatter.format(parser.parse(this)!!)
    } catch (ex: Exception) {
        ""
    }
}

fun Long.parseTo(to: String): String {
    return SimpleDateFormat(to, Locale.getDefault()).format(Date(this))
}