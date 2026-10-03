package cl.alkewallet.util

import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

/** Traduce excepciones técnicas a fallos con mensajes claros para el usuario. */
object ErrorMapper {

    fun map(error: Throwable): AppResult.Failure = when (error) {
        is HttpException -> fromHttp(error.code())
        is SocketTimeoutException -> AppResult.Failure(Messages.TIMEOUT, ErrorKind.NETWORK)
        is IOException -> AppResult.Failure(Messages.NETWORK, ErrorKind.NETWORK)
        is JsonParseException -> AppResult.Failure(Messages.BAD_RESPONSE, ErrorKind.SERVER)
        else -> AppResult.Failure(Messages.UNKNOWN, ErrorKind.UNKNOWN)
    }

    fun fromHttp(code: Int): AppResult.Failure = when (code) {
        400, 422 -> AppResult.Failure(Messages.VALIDATION, ErrorKind.VALIDATION)
        401, 403 -> AppResult.Failure(Messages.UNAUTHORIZED, ErrorKind.UNAUTHORIZED)
        408 -> AppResult.Failure(Messages.TIMEOUT, ErrorKind.NETWORK)
        409 -> AppResult.Failure(Messages.CONFLICT, ErrorKind.CONFLICT)
        in 500..599 -> AppResult.Failure(Messages.SERVER, ErrorKind.SERVER)
        else -> AppResult.Failure(Messages.UNKNOWN, ErrorKind.UNKNOWN)
    }
}

/** Ejecuta una llamada a la API y devuelve [AppResult] en vez de lanzar excepciones. */
suspend fun <T> safeApiCall(block: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        ErrorMapper.map(e)
    }

/** Ejecuta una operación sobre Room y devuelve [AppResult] en vez de lanzar excepciones. */
suspend fun <T> safeDbCall(block: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppResult.Failure(Messages.LOCAL, ErrorKind.LOCAL)
    }
