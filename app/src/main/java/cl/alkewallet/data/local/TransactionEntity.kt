package cl.alkewallet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.abs

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val description: String,
    val date: String,
    val type: String?
) {
    /** Un movimiento es egreso si el monto es negativo o el tipo indica salida de dinero. */
    fun isExpense(): Boolean =
        amount < 0 || type?.trim()?.lowercase() in EXPENSE_TYPES

    fun absoluteAmount(): Double = abs(amount)

    private companion object {
        val EXPENSE_TYPES = setOf("expense", "sent", "send", "debit", "egreso", "envio", "envío")
    }
}
