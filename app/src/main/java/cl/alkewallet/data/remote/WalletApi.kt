package cl.alkewallet.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Contrato con la API REST. Ajusta rutas y campos según la documentación oficial de tu API.
 */
interface WalletApi {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("users/me")
    suspend fun getProfile(): UserDto

    @GET("transactions")
    suspend fun getTransactions(): List<TransactionDto>

    @POST("transactions")
    suspend fun createTransaction(@Body body: NewTransactionRequest): TransactionDto
}
