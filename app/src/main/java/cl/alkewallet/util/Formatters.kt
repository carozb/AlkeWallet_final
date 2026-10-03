package cl.alkewallet.util

import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

object Formatters {
    private val locale = Locale("es", "CL")

    private fun number(value: Double): String =
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }.format(abs(value))

    /** Ej: $1.500,5 */
    fun money(amount: Double): String = (if (amount < 0) "-" else "") + "$" + number(amount)

    /** Ej: +$1.500 o -$1.500 según si el movimiento es ingreso o egreso. */
    fun signedMoney(amount: Double, expense: Boolean): String =
        (if (expense) "-" else "+") + "$" + number(amount)

    /** Convierte fechas ISO (2024-05-01T10:30:00...) a dd/MM/yyyy HH:mm. Si no puede, devuelve el texto original. */
    fun date(iso: String): String {
        val clean = iso.trim()
        val formats = listOf(
            Triple("yyyy-MM-dd'T'HH:mm:ss", 19, "dd/MM/yyyy HH:mm"),
            Triple("yyyy-MM-dd", 10, "dd/MM/yyyy")
        )
        for ((pattern, length, output) in formats) {
            if (clean.length < length) continue
            try {
                val parsed = SimpleDateFormat(pattern, Locale.US).parse(clean.substring(0, length))
                if (parsed != null) return SimpleDateFormat(output, locale).format(parsed)
            } catch (e: ParseException) {
                // se prueba el siguiente formato
            }
        }
        return clean
    }
}
