package br.ufes.passagens.model;

import java.io.File;
import java.math.BigDecimal;

public record VendaConfirmada(
        long vendaId,
        String numeroETicket,
        File arquivoPdf,
        BigDecimal valorTotal
) {
}
