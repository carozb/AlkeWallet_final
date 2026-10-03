package cl.alkewallet.data.remote

import cl.alkewallet.data.local.TransactionEntity
import cl.alkewallet.data.local.UserEntity
import com.google.gson.JsonParseException

// ---- Solicitudes ----
data class RegisterRequest(val username: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class NewTransactionRequest(val amount: Double, val description: String?)

// ---- Respuestas (campos nulables: Gson no respeta la nulabilidad de Kotlin) ----
data class AuthResponse(val token: String?, val user: UserDto?)

data class UserDto(
    val id: String?,
    val username: String?,
    val email: String?,
    val avatarUrl: String?,
    val balance: Double?
)

data class TransactionDto(
    val id: String?,
    val amount: Double?,
    val description: String?,
    val date: String?,
    val type: String?
)

// ---- Mapeo DTO -> entidad Room ----
fun UserDto.toEntity() = UserEntity(
    id = id ?: throw JsonParseException("Falta id de usuario"),
    username = username ?: email.orEmpty(),
    email = email.orEmpty(),
    avatarUrl = avatarUrl,
    balance = balance ?: 0.0
)

fun TransactionDto.toEntity() = TransactionEntity(
    id = id ?: throw JsonParseException("Falta id de transacción"),
    amount = amount ?: throw JsonParseException("Falta monto"),
    description = description.orEmpty(),
    date = date.orEmpty(),
    type = type
)
