package cl.alkewallet.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Guarda el token de sesión cifrado (AES-256, clave en Android Keystore). */
class SessionManager(context: Context) : TokenStore {

    private val prefs: SharedPreferences = createPrefs(context.applicationContext)

    override fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    override fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    override fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val FILE_NAME = "alke_session"
        const val KEY_TOKEN = "auth_token"

        fun createPrefs(context: Context): SharedPreferences {
            fun build(): SharedPreferences {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                return EncryptedSharedPreferences.create(
                    context,
                    FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            }
            return try {
                build()
            } catch (e: Exception) {
                // Archivo o clave corrupta (ocurre tras restaurar copias): se recrea y el usuario vuelve a iniciar sesión.
                context.deleteSharedPreferences(FILE_NAME)
                build()
            }
        }
    }
}
