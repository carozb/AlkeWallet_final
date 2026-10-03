package cl.alkewallet.data.repository

import cl.alkewallet.util.AppResult

interface AuthRepository {
    suspend fun login(email: String, password: String): AppResult<Unit>
    suspend fun register(username: String, email: String, password: String): AppResult<Unit>
    fun isLoggedIn(): Boolean
    suspend fun logout()
}
