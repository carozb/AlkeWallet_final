package cl.alkewallet.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    @Test
    fun correo_valido_e_invalido() {
        assertNull(Validators.email("ana@correo.com"))
        assertEquals(Messages.EMAIL_EMPTY, Validators.email("  "))
        assertEquals(Messages.EMAIL_INVALID, Validators.email("ana@correo"))
        assertEquals(Messages.EMAIL_INVALID, Validators.email("ana.correo.com"))
    }

    @Test
    fun contrasena_nueva_exige_largo_letra_y_numero() {
        assertNull(Validators.newPassword("clave1234"))
        assertNotNull(Validators.newPassword("corta1"))
        assertNotNull(Validators.newPassword("sololetras"))
        assertNotNull(Validators.newPassword("12345678"))
    }

    @Test
    fun usuario_entre_3_y_30_caracteres() {
        assertNull(Validators.username("ana_01"))
        assertNotNull(Validators.username("ab"))
        assertNotNull(Validators.username("a".repeat(31)))
        assertNotNull(Validators.username("nombre con espacios!"))
    }

    @Test
    fun monto_acepta_punto_o_coma_y_hasta_dos_decimales() {
        assertNull(Validators.amount("1500"))
        assertNull(Validators.amount("1500,50"))
        assertNull(Validators.amount("1500.5"))
        assertNotNull(Validators.amount("10.555"))
        assertNotNull(Validators.amount("-5"))
        assertNotNull(Validators.amount("0"))
        assertNotNull(Validators.amount(""))
    }

    @Test
    fun parseAmount_normaliza_la_coma() {
        assertEquals(1500.5, Validators.parseAmount("1500,5")!!, 0.0001)
    }

    @Test
    fun descripcion_maximo_100_caracteres() {
        assertNull(Validators.description("a".repeat(100)))
        assertNotNull(Validators.description("a".repeat(101)))
    }
}
