package cl.alkewallet.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cl.alkewallet.ui.auth.AuthViewModel
import cl.alkewallet.util.AppResult
import cl.alkewallet.util.ErrorKind
import cl.alkewallet.util.Messages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantExecutor = InstantTaskExecutorRule()

    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeAuthRepository()
        viewModel = AuthViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_con_correo_invalido_muestra_error_y_no_llama_al_repositorio() {
        viewModel.login("correo-malo", "12345678a")

        assertEquals(Messages.EMAIL_INVALID, viewModel.errors.value?.email)
        assertEquals(0, repository.loginCalls)
    }

    @Test
    fun login_con_contrasena_vacia_muestra_error() {
        viewModel.login("ana@correo.com", "")

        assertEquals(Messages.PASSWORD_EMPTY, viewModel.errors.value?.password)
        assertEquals(0, repository.loginCalls)
    }

    @Test
    fun login_exitoso_emite_evento_authenticated() {
        viewModel.login("ana@correo.com", "clave1234")

        assertEquals(1, repository.loginCalls)
        assertNotNull(viewModel.authenticated.value?.getContentIfNotHandled())
        assertEquals(false, viewModel.loading.value)
    }

    @Test
    fun login_con_credenciales_invalidas_emite_mensaje_claro() {
        repository.loginResult = AppResult.Failure(Messages.UNAUTHORIZED, ErrorKind.UNAUTHORIZED)

        viewModel.login("ana@correo.com", "clave1234")

        assertEquals(Messages.UNAUTHORIZED, viewModel.message.value?.getContentIfNotHandled())
        assertNull(viewModel.authenticated.value)
    }

    @Test
    fun registro_con_contrasenas_distintas_muestra_error() {
        viewModel.register("ana", "ana@correo.com", "clave1234", "otraClave99")

        assertEquals(Messages.CONFIRM_MISMATCH, viewModel.errors.value?.confirmPassword)
        assertEquals(0, repository.registerCalls)
    }

    @Test
    fun registro_con_contrasena_debil_muestra_error() {
        viewModel.register("ana", "ana@correo.com", "abc", "abc")

        assertEquals(Messages.PASSWORD_WEAK, viewModel.errors.value?.password)
        assertEquals(0, repository.registerCalls)
    }

    @Test
    fun registro_valido_llama_al_repositorio_y_autentica() {
        viewModel.register("ana_01", "ana@correo.com", "clave1234", "clave1234")

        assertEquals(1, repository.registerCalls)
        assertNotNull(viewModel.authenticated.value?.getContentIfNotHandled())
    }

    @Test
    fun registro_con_correo_duplicado_emite_mensaje_de_conflicto() {
        repository.registerResult = AppResult.Failure(Messages.CONFLICT, ErrorKind.CONFLICT)

        viewModel.register("ana_01", "ana@correo.com", "clave1234", "clave1234")

        assertEquals(Messages.CONFLICT, viewModel.message.value?.getContentIfNotHandled())
    }
}
