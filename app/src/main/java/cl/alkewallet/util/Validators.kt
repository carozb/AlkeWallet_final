package cl.alkewallet.util

/** Validaciones de formularios. Cada función devuelve el mensaje de error o null si es válido. */
object Validators {
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val USERNAME = Regex("^[\\p{L}0-9._-]{3,30}$")
    private val AMOUNT = Regex("^\\d{1,9}([.,]\\d{1,2})?$")

    fun email(value: String): String? = when {
        value.isBlank() -> Messages.EMAIL_EMPTY
        !EMAIL.matches(value.trim()) -> Messages.EMAIL_INVALID
        else -> null
    }

    /** En el inicio de sesión solo se exige que no esté vacía. */
    fun loginPassword(value: String): String? =
        if (value.isEmpty()) Messages.PASSWORD_EMPTY else null

    /** En el registro se exige una contraseña mínima: 8 caracteres, una letra y un número. */
    fun newPassword(value: String): String? = when {
        value.length < 8 || value.none { it.isLetter() } || value.none { it.isDigit() } -> Messages.PASSWORD_WEAK
        else -> null
    }

    fun confirmPassword(password: String, confirm: String): String? =
        if (password != confirm) Messages.CONFIRM_MISMATCH else null

    fun username(value: String): String? =
        if (!USERNAME.matches(value.trim())) Messages.USERNAME_INVALID else null

    fun amount(text: String): String? = when {
        text.isBlank() -> Messages.AMOUNT_EMPTY
        !AMOUNT.matches(text.trim()) -> Messages.AMOUNT_INVALID
        (parseAmount(text) ?: 0.0) <= 0.0 -> Messages.AMOUNT_INVALID
        else -> null
    }

    fun parseAmount(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()

    fun description(value: String): String? =
        if (value.trim().length > 100) Messages.DESCRIPTION_TOO_LONG else null
}
