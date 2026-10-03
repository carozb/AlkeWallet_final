package cl.alkewallet.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.alkewallet.data.repository.AuthRepository
import cl.alkewallet.util.AppResult
import cl.alkewallet.util.Event
import cl.alkewallet.util.Validators
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    data class FormErrors(
        val username: String? = null,
        val email: String? = null,
        val password: String? = null,
        val confirmPassword: String? = null
    ) {
        val hasErrors: Boolean
            get() = username != null || email != null || password != null || confirmPassword != null
    }

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _errors = MutableLiveData(FormErrors())
    val errors: LiveData<FormErrors> = _errors

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    private val _authenticated = MutableLiveData<Event<Unit>>()
    val authenticated: LiveData<Event<Unit>> = _authenticated

    fun isLoggedIn(): Boolean = repository.isLoggedIn()

    fun login(email: String, password: String) {
        val errors = FormErrors(
            email = Validators.email(email),
            password = Validators.loginPassword(password)
        )
        _errors.value = errors
        if (errors.hasErrors) return
        execute { repository.login(email, password) }
    }

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        val errors = FormErrors(
            username = Validators.username(username),
            email = Validators.email(email),
            password = Validators.newPassword(password),
            confirmPassword = Validators.confirmPassword(password, confirmPassword)
        )
        _errors.value = errors
        if (errors.hasErrors) return
        execute { repository.register(username, email, password) }
    }

    private fun execute(call: suspend () -> AppResult<Unit>) {
        if (_loading.value == true) return
        viewModelScope.launch {
            _loading.value = true
            when (val result = call()) {
                is AppResult.Success -> _authenticated.value = Event(Unit)
                is AppResult.Failure -> _message.value = Event(result.message)
            }
            _loading.value = false
        }
    }
}
