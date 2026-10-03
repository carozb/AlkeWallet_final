package cl.alkewallet.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cl.alkewallet.ui.main.WalletViewModel
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
class WalletViewModelTest {

    @get:Rule
    val instantExecutor = InstantTaskExecutorRule()

    private lateinit var wallet: FakeWalletRepository
    private lateinit var auth: FakeAuthRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        wallet = FakeWalletRepository()
        auth = FakeAuthRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = WalletViewModel(wallet, auth)

    @Test
    fun al_iniciar_sincroniza_con_la_api() {
        val viewModel = createViewModel()

        assertEquals(1, wallet.refreshCalls)
        assertEquals(false, viewModel.offline.value)
        assertEquals(false, viewModel.loading.value)
    }

    @Test
    fun sin_conexion_activa_modo_offline_sin_mensaje_de_error() {
        wallet.refreshResult = AppResult.Failure(Messages.NETWORK, ErrorKind.NETWORK)

        val viewModel = createViewModel()

        assertEquals(true, viewModel.offline.value)
        assertNull(viewModel.message.value)
    }

    @Test
    fun error_de_servidor_en_refresh_emite_mensaje() {
        wallet.refreshResult = AppResult.Failure(Messages.SERVER, ErrorKind.SERVER)

        val viewModel = createViewModel()

        assertEquals(Messages.SERVER, viewModel.message.value?.getContentIfNotHandled())
    }

    @Test
    fun sesion_expirada_cierra_sesion_y_notifica() {
        wallet.refreshResult = AppResult.Failure(Messages.UNAUTHORIZED, ErrorKind.UNAUTHORIZED)

        val viewModel = createViewModel()

        assertEquals(1, auth.logoutCalls)
        assertNotNull(viewModel.loggedOut.value?.getContentIfNotHandled())
        assertEquals(Messages.SESSION_EXPIRED, viewModel.message.value?.getContentIfNotHandled())
    }

    @Test
    fun enviar_con_monto_invalido_no_llama_al_repositorio() {
        val viewModel = createViewModel()

        viewModel.send("abc", "")

        assertEquals(Messages.AMOUNT_INVALID, viewModel.sendErrors.value?.amount)
        assertEquals(0, wallet.sentRequests.size)
    }

    @Test
    fun enviar_con_monto_cero_es_invalido() {
        val viewModel = createViewModel()

        viewModel.send("0", "")

        assertEquals(Messages.AMOUNT_INVALID, viewModel.sendErrors.value?.amount)
    }

    @Test
    fun enviar_valido_llama_al_repositorio_y_emite_exito() {
        val viewModel = createViewModel()

        viewModel.send("1500,50", "  Pago almuerzo  ")

        assertEquals(listOf(1500.5 to "Pago almuerzo"), wallet.sentRequests)
        assertNotNull(viewModel.sent.value?.getContentIfNotHandled())
        assertEquals(Messages.TRANSACTION_OK, viewModel.message.value?.getContentIfNotHandled())
        assertEquals(false, viewModel.sending.value)
    }

    @Test
    fun descripcion_vacia_se_envia_como_nula() {
        val viewModel = createViewModel()

        viewModel.send("1000", "   ")

        assertEquals(listOf(1000.0 to null), wallet.sentRequests)
    }

    @Test
    fun error_al_enviar_emite_mensaje_y_no_marca_exito() {
        wallet.sendResult = AppResult.Failure(Messages.NETWORK, ErrorKind.NETWORK)
        val viewModel = createViewModel()

        viewModel.send("1000", "")

        assertEquals(Messages.NETWORK, viewModel.message.value?.getContentIfNotHandled())
        assertNull(viewModel.sent.value)
    }

    @Test
    fun logout_limpia_sesion_y_notifica() {
        val viewModel = createViewModel()

        viewModel.logout()

        assertEquals(1, auth.logoutCalls)
        assertNotNull(viewModel.loggedOut.value?.getContentIfNotHandled())
    }
}
