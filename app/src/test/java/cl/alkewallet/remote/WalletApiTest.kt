package cl.alkewallet.remote

import cl.alkewallet.data.remote.LoginRequest
import cl.alkewallet.data.remote.NewTransactionRequest
import cl.alkewallet.data.remote.RetrofitClient
import cl.alkewallet.data.remote.WalletApi
import cl.alkewallet.data.remote.toEntity
import cl.alkewallet.util.AppResult
import cl.alkewallet.util.ErrorKind
import cl.alkewallet.util.Messages
import cl.alkewallet.util.safeApiCall
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Pruebas de integración de Retrofit contra un servidor simulado (MockWebServer). */
class WalletApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: WalletApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = RetrofitClient.create(server.url("/").toString(), { "token-123" }, debug = false)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun login_envia_POST_y_parsea_token_y_usuario() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """{"token":"abc","user":{"id":"1","username":"ana","email":"ana@correo.com","avatarUrl":"https://x.cl/a.png","balance":1500.5}}"""
            )
        )

        val response = api.login(LoginRequest("ana@correo.com", "clave1234"))
        val request = server.takeRequest()

        assertEquals("POST", request.method)
        assertEquals("/auth/login", request.path)
        assertTrue(request.body.readUtf8().contains("ana@correo.com"))
        assertEquals("abc", response.token)
        assertEquals("ana", response.user?.toEntity()?.username)
        assertEquals(1500.5, response.user?.toEntity()?.balance ?: 0.0, 0.0001)
    }

    @Test
    fun getTransactions_envia_token_bearer_y_parsea_lista() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """[{"id":"10","amount":-2500,"description":"Café","date":"2024-05-01T10:30:00Z","type":"expense"},
                    {"id":"11","amount":10000,"description":null,"date":"2024-05-02T09:00:00Z","type":"income"}]"""
            )
        )

        val list = api.getTransactions().map { it.toEntity() }
        val request = server.takeRequest()

        assertEquals("GET", request.method)
        assertEquals("/transactions", request.path)
        assertEquals("Bearer token-123", request.getHeader("Authorization"))
        assertEquals(2, list.size)
        assertTrue(list[0].isExpense())
        assertEquals("", list[1].description)
    }

    @Test
    fun createTransaction_envia_monto_y_descripcion_en_el_cuerpo() = runBlocking {
        server.enqueue(
            MockResponse().setBody("""{"id":"99","amount":1500.5,"description":"Pago","date":"2024-05-03T12:00:00Z"}""")
        )

        val created = api.createTransaction(NewTransactionRequest(1500.5, "Pago")).toEntity()
        val request = server.takeRequest()
        val body = request.body.readUtf8()

        assertEquals("POST", request.method)
        assertEquals("/transactions", request.path)
        assertTrue(body.contains("1500.5"))
        assertTrue(body.contains("Pago"))
        assertEquals("99", created.id)
    }

    @Test
    fun error_401_se_traduce_a_mensaje_de_no_autorizado() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401))

        val result = safeApiCall { api.getProfile() }

        assertTrue(result is AppResult.Failure)
        result as AppResult.Failure
        assertEquals(ErrorKind.UNAUTHORIZED, result.kind)
        assertEquals(Messages.UNAUTHORIZED, result.message)
    }

    @Test
    fun error_409_se_traduce_a_conflicto() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(409))

        val result = safeApiCall { api.getProfile() } as AppResult.Failure

        assertEquals(ErrorKind.CONFLICT, result.kind)
    }

    @Test
    fun error_500_se_traduce_a_servicio_no_disponible() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = safeApiCall { api.getTransactions() } as AppResult.Failure

        assertEquals(ErrorKind.SERVER, result.kind)
        assertEquals(Messages.SERVER, result.message)
    }

    @Test
    fun respuesta_malformada_se_traduce_a_respuesta_inesperada() = runBlocking {
        server.enqueue(MockResponse().setBody("<html>no es json</html>"))

        val result = safeApiCall { api.getProfile() } as AppResult.Failure

        assertEquals(Messages.BAD_RESPONSE, result.message)
    }

    @Test
    fun usuario_sin_id_se_considera_respuesta_invalida() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"username":"ana"}"""))

        val result = safeApiCall { api.getProfile().toEntity() } as AppResult.Failure

        assertEquals(Messages.BAD_RESPONSE, result.message)
    }

    @Test
    fun sin_conexion_se_traduce_a_error_de_red() = runBlocking {
        val offlineApi = RetrofitClient.create("http://localhost:1/", { null }, debug = false)

        val result = safeApiCall { offlineApi.getProfile() } as AppResult.Failure

        assertEquals(ErrorKind.NETWORK, result.kind)
        assertEquals(Messages.NETWORK, result.message)
    }
}
