package cl.alkewallet.viewmodel

import cl.alkewallet.data.local.TransactionEntity
import cl.alkewallet.data.local.UserEntity
import cl.alkewallet.data.repository.AuthRepository
import cl.alkewallet.data.repository.WalletRepository
import cl.alkewallet.util.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository : AuthRepository {
    var loginResult: AppResult<Unit> = AppResult.Success(Unit)
    var registerResult: AppResult<Unit> = AppResult.Success(Unit)
    var loggedIn = false
    var loginCalls = 0
    var registerCalls = 0
    var logoutCalls = 0

    override suspend fun login(email: String, password: String): AppResult<Unit> {
        loginCalls++
        return loginResult
    }

    override suspend fun register(username: String, email: String, password: String): AppResult<Unit> {
        registerCalls++
        return registerResult
    }

    override fun isLoggedIn(): Boolean = loggedIn

    override suspend fun logout() {
        logoutCalls++
    }
}

class FakeWalletRepository : WalletRepository {
    val user = MutableStateFlow<UserEntity?>(null)
    val txs = MutableStateFlow<List<TransactionEntity>>(emptyList())

    override val profile: Flow<UserEntity?> = user
    override val transactions: Flow<List<TransactionEntity>> = txs

    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)
    var sendResult: AppResult<Unit> = AppResult.Success(Unit)
    var refreshCalls = 0
    val sentRequests = mutableListOf<Pair<Double, String?>>()

    override suspend fun refresh(): AppResult<Unit> {
        refreshCalls++
        return refreshResult
    }

    override suspend fun sendTransaction(amount: Double, description: String?): AppResult<Unit> {
        sentRequests += amount to description
        return sendResult
    }
}
