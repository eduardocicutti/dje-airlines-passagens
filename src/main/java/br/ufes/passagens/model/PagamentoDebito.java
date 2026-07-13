package br.ufes.passagens.model;

import java.math.BigDecimal;

public final class PagamentoDebito extends PagamentoCartao {
    public PagamentoDebito(BigDecimal valor, String numeroCartao, String bandeira) {
        super(valor, numeroCartao, bandeira);
    }

    @Override public TipoPagamento getTipo() { return TipoPagamento.DEBITO; }
    @Override public boolean processarPagamento() { return true; }
}
