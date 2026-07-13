package br.ufes.passagens.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public final class Passagem {
    private final Voo voo;
    private final Passageiro passageiro;
    private final String assento;
    private final LocalDate dataRetorno;
    private BigDecimal valorTotal;
    private String numeroETicket;

    public Passagem(Voo voo, Passageiro passageiro, String assento, LocalDate dataRetorno) {
        this.voo = Objects.requireNonNull(voo, "Voo obrigatório.");
        this.passageiro = Objects.requireNonNull(passageiro, "Passageiro obrigatório.");
        if (assento == null || !assento.trim().toUpperCase().matches("[A-Z][1-9][0-9]?")) {
            throw new IllegalArgumentException("Código de assento inválido.");
        }
        this.assento = assento.trim().toUpperCase();
        this.dataRetorno = dataRetorno;
        this.valorTotal = BigDecimal.ZERO;
    }

    public Voo getVoo() { return voo; }
    public Passageiro getPassageiro() { return passageiro; }
    public String getAssento() { return assento; }
    public LocalDate getDataRetorno() { return dataRetorno; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = Objects.requireNonNull(valorTotal); }
    public String getNumeroETicket() { return numeroETicket; }
    public void setNumeroETicket(String numeroETicket) { this.numeroETicket = numeroETicket; }
}
