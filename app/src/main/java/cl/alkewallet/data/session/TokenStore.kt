package cl.alkewallet.data.session

interface TokenStore {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clear()
    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()
}
