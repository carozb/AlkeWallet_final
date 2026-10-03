package cl.alkewallet.util

/** Mensajes comprensibles para el usuario (nunca se muestran errores técnicos). */
object Messages {
    const val NETWORK = "No pudimos conectarnos al servidor. Revisa tu conexión a internet e inténtalo nuevamente."
    const val TIMEOUT = "El servidor tardó demasiado en responder. Inténtalo nuevamente."
    const val UNAUTHORIZED = "Correo o contraseña incorrectos, o tu sesión expiró."
    const val SESSION_EXPIRED = "Tu sesión expiró. Inicia sesión nuevamente."
    const val VALIDATION = "Los datos enviados no son válidos. Revísalos e inténtalo de nuevo."
    const val CONFLICT = "Ya existe una cuenta registrada con esos datos."
    const val SERVER = "El servicio no está disponible en este momento. Inténtalo más tarde."
    const val BAD_RESPONSE = "Recibimos una respuesta inesperada del servidor."
    const val LOCAL = "No pudimos acceder a los datos guardados en tu dispositivo."
    const val UNKNOWN = "Ocurrió un error inesperado. Inténtalo nuevamente."

    const val TRANSACTION_OK = "Transacción realizada con éxito."

    // Validaciones de formularios
    const val EMAIL_EMPTY = "Ingresa tu correo electrónico."
    const val EMAIL_INVALID = "Ingresa un correo válido (ej: nombre@correo.com)."
    const val PASSWORD_EMPTY = "Ingresa tu contraseña."
    const val PASSWORD_WEAK = "La contraseña debe tener al menos 8 caracteres, una letra y un número."
    const val CONFIRM_MISMATCH = "Las contraseñas no coinciden."
    const val USERNAME_INVALID = "El nombre de usuario debe tener entre 3 y 30 caracteres (letras, números, . _ -)."
    const val AMOUNT_EMPTY = "Ingresa un monto."
    const val AMOUNT_INVALID = "Ingresa un monto válido mayor a 0, con hasta 2 decimales."
    const val DESCRIPTION_TOO_LONG = "La descripción no puede superar los 100 caracteres."
}
