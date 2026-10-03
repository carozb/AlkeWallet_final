package cl.alkewallet.data.repository

import androidx.room.withTransaction
import cl.alkewallet.data.local.AppDatabase
import cl.alkewallet.data.local.TransactionEntity
import cl.alkewallet.data.local.UserEntity
import cl.alkewallet.data.remote.NewTransactionRequest
import cl.alkewallet.data.remote.WalletApi
import cl.alkewallet.data.remote.toEntity
import cl.alkewallet.util.AppResult
import cl.alkewallet.util.safeApiCall
import cl.alkewallet.util.safeDbCall
import kotlinx.coroutines.flow.Flow

class WalletRepositoryImpl(
    private val api: WalletApi,
    private val db: AppDatabase
) : WalletRepository {

    override val profile: Flow<UserEntity?> = db.userDao().observeCurrent()
    override val transactions: Flow<List<TransactionEntity>> = db.transactionDao().observeAll()

    override suspend fun refresh(): AppResult<Unit> {
        val remote = safeApiCall {
            api.getProfile().toEntity() to api.getTransactions().map { it.toEntity() }
        }
        return when (remote) {
            is AppResult.Failure -> remote
            is AppResult.Success -> safeDbCall {
                val (user, list) = remote.data
                db.withTransaction {
                    db.userDao().upsert(user)
                    db.transactionDao().clear()
                    db.transactionDao().insertAll(list)
                }
            }
        }
    }

    override suspend fun sendTransaction(amount: Double, description: String?): AppResult<Unit> {
        val created = safeApiCall {
            api.createTransaction(NewTransactionRequest(amount, description)).toEntity()
        }
        return when (created) {
            is AppResult.Failure -> created
            is AppResult.Success -> {
                val saved = safeDbCall { db.transactionDao().insert(created.data) }
                refresh() // mejor esfuerzo: actualiza saldo e historial; si falla, queda lo ya guardado
                saved
            }
        }
    }
}
