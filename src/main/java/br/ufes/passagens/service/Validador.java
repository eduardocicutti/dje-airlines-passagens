package br.ufes.passagens.service;

import br.ufes.passagens.model.TipoPagamento;

import java.time.LocalDate;

public final class Validador {
    public boolean validarMatriculaFuncionario(String matricula, TipoPagamento tipo) {
        return tipo != TipoPagamento.DINHEIRO
                || (matricula != null && matricula.trim().matches("\\d{4,}"));
    }

    public boolean validarDataVoo(LocalDate data, LocalDate hoje) {
        return data != null && !data.isBefore(hoje);
    }

    public boolean podeAvancarEtapa(boolean etapaConcluidaSemErros) {
        return etapaConcluidaSemErros;
    }
}
