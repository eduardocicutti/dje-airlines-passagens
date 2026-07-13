package br.ufes.passagens.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PagamentoTest {
    @Test
    void processaAsTresFormasPolimorficamente() {
        List<Pagamento> pagamentos = List.of(
                new PagamentoDinheiro(new BigDecimal("100.00"), "1001"),
                new PagamentoCredito(new BigDecimal("100.00"), "4111111111111111", "Visa", 2),
                new PagamentoDebito(new BigDecimal("100.00"), "5555555555554444", "Mastercard"));

        assertTrue(pagamentos.stream().allMatch(Pagamento::processarPagamento));
        assertEquals(List.of(TipoPagamento.DINHEIRO, TipoPagamento.CREDITO, TipoPagamento.DEBITO),
                pagamentos.stream().map(Pagamento::getTipo).toList());
    }

    @Test
    void dinheiroExigeMatriculaValida() {
        assertThrows(IllegalArgumentException.class,
                () -> new PagamentoDinheiro(new BigDecimal("10.00"), "12A"));
    }

    @Test
    void cartaoGuardaSomenteFinalMascarado() {
        PagamentoDebito pagamento = new PagamentoDebito(
                new BigDecimal("10.00"), "4111111111111234", "Visa");
        assertEquals("1234", pagamento.getUltimosQuatroDigitos());
        assertEquals("**** **** **** 1234", pagamento.getNumeroMascarado());
    }
}
