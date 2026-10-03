package cl.alkewallet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.alkewallet.data.repository.AuthRepository
import cl.alkewallet.data.repository.WalletRepository
import cl.alkewallet.ui.auth.AuthViewModel
import cl.alkewallet.ui.main.WalletViewModel

class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val walletRepository: WalletRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(authRepository) as T
        modelClass.isAssignableFrom(WalletViewModel::class.java) ->
            WalletViewModel(walletRepository, authRepository) as T
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
