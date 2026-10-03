package cl.alkewallet.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.alkewallet.data.local.AppDatabase
import cl.alkewallet.data.local.TransactionDao
import cl.alkewallet.data.local.TransactionEntity
import cl.alkewallet.data.local.UserDao
import cl.alkewallet.data.local.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var transactionDao: TransactionDao
    private lateinit var userDao: UserDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        transactionDao = db.transactionDao()
        userDao = db.userDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    private fun tx(id: String, date: String, amount: Double = 100.0) =
        TransactionEntity(id, amount, "Movimiento $id", date, null)

    @Test
    fun insertAll_y_observeAll_ordena_por_fecha_descendente() = runBlocking {
        transactionDao.insertAll(
            listOf(
                tx("1", "2024-01-01T10:00:00"),
                tx("2", "2024-03-01T10:00:00"),
                tx("3", "2024-02-01T10:00:00")
            )
        )

        val result = transactionDao.observeAll().first()

        assertEquals(listOf("2", "3", "1"), result.map { it.id })
    }

    @Test
    fun getById_devuelve_la_transaccion_o_nulo() = runBlocking {
        transactionDao.insert(tx("1", "2024-01-01T10:00:00"))

        assertNotNull(transactionDao.getById("1"))
        assertNull(transactionDao.getById("no-existe"))
    }

    @Test
    fun update_modifica_la_transaccion() = runBlocking {
        transactionDao.insert(tx("1", "2024-01-01T10:00:00", amount = 100.0))

        transactionDao.update(tx("1", "2024-01-01T10:00:00", amount = 250.0))

        assertEquals(250.0, transactionDao.getById("1")!!.amount, 0.0001)
    }

    @Test
    fun delete_elimina_la_transaccion() = runBlocking {
        val item = tx("1", "2024-01-01T10:00:00")
        transactionDao.insert(item)

        transactionDao.delete(item)

        assertNull(transactionDao.getById("1"))
    }

    @Test
    fun insertar_el_mismo_id_reemplaza_el_registro() = runBlocking {
        transactionDao.insert(tx("1", "2024-01-01T10:00:00", amount = 100.0))
        transactionDao.insert(tx("1", "2024-01-01T10:00:00", amount = 999.0))

        val all = transactionDao.observeAll().first()

        assertEquals(1, all.size)
        assertEquals(999.0, all[0].amount, 0.0001)
    }

    @Test
    fun clear_vacia_la_tabla() = runBlocking {
        transactionDao.insertAll(listOf(tx("1", "2024-01-01T10:00:00"), tx("2", "2024-01-02T10:00:00")))

        transactionDao.clear()

        assertEquals(0, transactionDao.observeAll().first().size)
    }

    @Test
    fun usuario_upsert_update_y_clear() = runBlocking {
        val user = UserEntity("1", "ana", "ana@correo.com", null, 1000.0)

        userDao.upsert(user)
        assertEquals("ana", userDao.observeCurrent().first()?.username)

        userDao.update(user.copy(balance = 2500.0))
        assertEquals(2500.0, userDao.getCurrent()!!.balance, 0.0001)

        userDao.clear()
        assertNull(userDao.getCurrent())
    }
}
