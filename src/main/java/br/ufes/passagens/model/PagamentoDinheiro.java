package br.ufes.passagens.model;

import java.math.BigDecimal;

public final class PagamentoDinheiro extends Pagamento {
    private final String matriculaFuncionario;

    public PagamentoDinheiro(BigDecimal valor, String matriculaFuncionario) {
        super(valor);
        if (matriculaFuncionario == null || !matriculaFuncionario.trim().matches("\\d{4,}")) {
            throw new IllegalArgumentException("Matrícula inválida.");
        }
        this.matriculaFuncionario = matriculaFuncionario.trim();
    }

    public String getMatriculaFuncionario() { return matriculaFuncionario; }
    @Override public TipoPagamento getTipo() { return TipoPagamento.DINHEIRO; }
    @Override public boolean processarPagamento() { return true; }
}
