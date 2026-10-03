package cl.alkewallet.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import cl.alkewallet.data.local.TransactionEntity
import cl.alkewallet.data.local.UserEntity
import cl.alkewallet.data.repository.AuthRepository
import cl.alkewallet.data.repository.WalletRepository
import cl.alkewallet.util.AppResult
import cl.alkewallet.util.ErrorKind
import cl.alkewallet.util.Event
import cl.alkewallet.util.Messages
import cl.alkewallet.util.Validators
import kotlinx.coroutines.launch

/**
 * Coordina la API (Retrofit) y la base local (Room) para las pantallas principales.
 * La vista solo observa los LiveData expuestos aquí.
 */
class WalletViewModel(
    private val wallet: WalletRepository,
    private val auth: AuthRepository
) : ViewModel() {

    data class SendErrors(val amount: String? = null, val description: String? = null) {
        val hasErrors: Boolean get() = amount != null || description != null
    }

    // Datos locales (Room) -> disponibles sin conexión
    val profile: LiveData<UserEntity?> = wallet.profile.asLiveData()
    val transactions: LiveData<List<TransactionEntity>> = wallet.transactions.asLiveData()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _sending = MutableLiveData(false)
    val sending: LiveData<Boolean> = _sending

    private val _offline = MutableLiveData(false)
    val offline: LiveData<Boolean> = _offline

    private val _sendErrors = MutableLiveData(SendErrors())
    val sendErrors: LiveData<SendErrors> = _sendErrors

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    private val _sent = MutableLiveData<Event<Unit>>()
    val sent: LiveData<Event<Unit>> = _sent

    private val _loggedOut = MutableLiveData<Event<Unit>>()
    val loggedOut: LiveData<Event<Unit>> = _loggedOut

    init {
        refresh()
    }

    fun refresh() {
        if (_loading.value == true) return
        viewModelScope.launch {
            _loading.value = true
            when (val result = wallet.refresh()) {
                is AppResult.Success -> _offline.value = false
                is AppResult.Failure -> handleFailure(result, networkAsBanner = true)
            }
            _loading.value = false
        }
    }

    fun send(amountText: String, description: String) {
        val errors = SendErrors(
            amount = Validators.amount(amountText),
            description = Validators.description(description)
        )
        _sendErrors.value = errors
        if (errors.hasErrors || _sending.value == true) return
        val amount = Validators.parseAmount(amountText) ?: return

        viewModelScope.launch {
            _sending.value = true
            when (val result = wallet.sendTransaction(amount, description.trim().ifBlank { null })) {
                is AppResult.Success -> {
                    _sendErrors.value = SendErrors()
                    _message.value = Event(Messages.TRANSACTION_OK)
                    _sent.value = Event(Unit)
                }
                is AppResult.Failure -> handleFailure(result, networkAsBanner = false)
            }
            _sending.value = false
        }
    }

    fun logout() {
        viewModelScope.launch { closeSession() }
    }

    private suspend fun handleFailure(failure: AppResult.Failure, networkAsBanner: Boolean) {
        when (failure.kind) {
            ErrorKind.UNAUTHORIZED -> {
                _message.value = Event(Messages.SESSION_EXPIRED)
                closeSession()
            }
            ErrorKind.NETWORK ->
                if (networkAsBanner) _offline.value = true else _message.value = Event(failure.message)
            else -> _message.value = Event(failure.message)
        }
    }

    private suspend fun closeSession() {
        auth.logout()
        _loggedOut.value = Event(Unit)
    }
}
