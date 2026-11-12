package util;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValidadorServicioTest {

    @Test
    public void duracionPositivaEsValida() {
        assertTrue(ValidadorServicio.duracionValida(45));
    }

    @Test
    public void duracionCeroEsInvalida() {
        assertFalse(ValidadorServicio.duracionValida(0));
    }

    @Test
    public void duracionNegativaEsInvalida() {
        assertFalse(ValidadorServicio.duracionValida(-10));
    }
}