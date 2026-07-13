package br.ufes.passagens.model;

import java.math.BigDecimal;

public final class PagamentoCredito extends PagamentoCartao {
    private final int parcelas;

    public PagamentoCredito(BigDecimal valor, String numeroCartao, String bandeira, int parcelas) {
        super(valor, numeroCartao, bandeira);
        if (parcelas < 1 || parcelas > 12) {
            throw new IllegalArgumentException("Parcelas devem estar entre 1 e 12.");
        }
        this.parcelas = parcelas;
    }

    public int getParcelas() { return parcelas; }
    @Override public TipoPagamento getTipo() { return TipoPagamento.CREDITO; }
    @Override public boolean processarPagamento() { return true; }
}
