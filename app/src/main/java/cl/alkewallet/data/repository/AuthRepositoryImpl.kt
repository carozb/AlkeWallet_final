package cl.alkewallet.data.repository

import androidx.room.withTransaction
import cl.alkewallet.data.local.AppDatabase
import cl.alkewallet.data.remote.AuthResponse
import cl.alkewallet.data.remote.LoginRequest
import cl.alkewallet.data.remote.RegisterRequest
import cl.alkewallet.data.remote.WalletApi
import cl.alkewallet.data.remote.toEntity
import cl.alkewallet.data.session.TokenStore
import cl.alkewallet.util.AppResult
import cl.alkewallet.util.safeApiCall
import cl.alkewallet.util.safeDbCall
import com.google.gson.JsonParseException

class AuthRepositoryImpl(
    private val api: WalletApi,
    private val session: TokenStore,
    private val db: AppDatabase
) : AuthRepository {

    override suspend fun login(email: String, password: String): AppResult<Unit> =
        authenticate { api.login(LoginRequest(email.trim(), password)) }

    override suspend fun register(username: String, email: String, password: String): AppResult<Unit> {
        // Se crea la cuenta y luego se inicia sesión, así funciona aunque la API no devuelva token al registrar.
        val created = safeApiCall { api.register(RegisterRequest(username.trim(), email.trim(), password)) }
        if (created is AppResult.Failure) return created
        return login(email, password)
    }

    override fun isLoggedIn(): Boolean = session.isLoggedIn()

    override suspend fun logout() {
        session.clear()
        safeDbCall {
            db.withTransaction {
                db.userDao().clear()
                db.transactionDao().clear()
            }
        }
    }

    private suspend fun authenticate(call: suspend () -> AuthResponse): AppResult<Unit> {
        val remote = safeApiCall {
            val response = call()
            val token = response.token ?: throw JsonParseException("Falta token")
            session.saveToken(token)
            (response.user ?: api.getProfile()).toEntity()
        }
        return when (remote) {
            is AppResult.Failure -> {
                session.clear()
                remote
            }
            is AppResult.Success -> {
                val saved = safeDbCall { db.userDao().upsert(remote.data) }
                if (saved is AppResult.Failure) session.clear()
                saved
            }
        }
    }
}
