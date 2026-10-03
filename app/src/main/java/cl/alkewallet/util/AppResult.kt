package cl.alkewallet.util

/** Tipos de error que la UI y los ViewModels saben interpretar. */
enum class ErrorKind { NETWORK, UNAUTHORIZED, VALIDATION, CONFLICT, SERVER, LOCAL, UNKNOWN }

/** Resultado de una operación: éxito con datos o fallo con un mensaje listo para el usuario. */
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Failure(
        val message: String,
        val kind: ErrorKind = ErrorKind.UNKNOWN
    ) : AppResult<Nothing>()
}
