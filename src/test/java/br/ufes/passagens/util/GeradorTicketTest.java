package br.ufes.passagens.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeradorTicketTest {
    @Test
    void geraCodigoEstavelComDozeHexadecimais() {
        String a = GeradorTicket.gerarNumeroETicket(
                "Ana Lima", "12345678900", "DJE101", "20/07/2026", 42L);
        String b = GeradorTicket.gerarNumeroETicket(
                "Ana Lima", "12345678900", "DJE101", "20/07/2026", 42L);
        assertEquals(a, b);
        assertTrue(a.matches("[0-9A-F]{12}"));
    }

    @Test
    void vendaDiferenteProduzOutroTicket() {
        String a = GeradorTicket.gerarNumeroETicket("Ana", "1", "DJE101", "20/07/2026", 1L);
        String b = GeradorTicket.gerarNumeroETicket("Ana", "1", "DJE101", "20/07/2026", 2L);
        assertNotEquals(a, b);
    }
}
