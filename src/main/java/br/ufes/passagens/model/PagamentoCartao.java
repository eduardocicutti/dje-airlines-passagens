package br.ufes.passagens.model;

import java.math.BigDecimal;

public abstract class PagamentoCartao extends Pagamento {
    private final String ultimosQuatroDigitos;
    private final String bandeira;

    protected PagamentoCartao(BigDecimal valorTotal, String numeroCartao, String bandeira) {
        super(valorTotal);
        String somenteDigitos = numeroCartao == null ? "" : numeroCartao.replaceAll("\\D", "");
        if (somenteDigitos.length() < 13 || somenteDigitos.length() > 19) {
            throw new IllegalArgumentException("Número do cartão inválido.");
        }
        if (bandeira == null || bandeira.isBlank()) {
            throw new IllegalArgumentException("Bandeira do cartão obrigatória.");
        }
        this.ultimosQuatroDigitos = somenteDigitos.substring(somenteDigitos.length() - 4);
        this.bandeira = bandeira.trim();
    }

    public String getUltimosQuatroDigitos() { return ultimosQuatroDigitos; }
    public String getBandeira() { return bandeira; }
    public String getNumeroMascarado() { return "**** **** **** " + ultimosQuatroDigitos; }
}
