package cl.alkewallet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Perfil del usuario guardado para uso sin conexión. No almacena contraseñas ni tokens. */
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val avatarUrl: String?,
    val balance: Double
)
