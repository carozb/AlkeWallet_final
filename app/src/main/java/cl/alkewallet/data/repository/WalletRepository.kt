package cl.alkewallet.data.repository

import cl.alkewallet.data.local.TransactionEntity
import cl.alkewallet.data.local.UserEntity
import cl.alkewallet.util.AppResult
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    /** Perfil local (Room). Disponible sin conexión. */
    val profile: Flow<UserEntity?>

    /** Historial local (Room), más reciente primero. Disponible sin conexión. */
    val transactions: Flow<List<TransactionEntity>>

    /** Descarga perfil e historial desde la API y los guarda en Room. */
    suspend fun refresh(): AppResult<Unit>

    /** Envía una transacción a la API y actualiza la base local. */
    suspend fun sendTransaction(amount: Double, description: String?): AppResult<Unit>
}
