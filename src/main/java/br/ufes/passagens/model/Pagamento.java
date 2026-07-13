package br.ufes.passagens.model;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class Pagamento {
    private final BigDecimal valorTotal;

    protected Pagamento(BigDecimal valorTotal) {
        this.valorTotal = Objects.requireNonNull(valorTotal, "Valor obrigatório.");
        if (valorTotal.signum() <= 0) {
            throw new IllegalArgumentException("O valor do pagamento deve ser positivo.");
        }
    }

    public BigDecimal getValorTotal() { return valorTotal; }
    public abstract TipoPagamento getTipo();
    public abstract boolean processarPagamento();
}
