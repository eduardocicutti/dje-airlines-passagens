package br.ufes.passagens.model;

import java.util.Locale;
import java.util.Objects;

public final class Aeroporto {
    private final String codigoIata;
    private final String cidade;
    private final String nome;

    public Aeroporto(String codigoIata, String cidade, String nome) {
        this.codigoIata = validarCodigo(codigoIata);
        this.cidade = exigirTexto(cidade, "cidade");
        this.nome = exigirTexto(nome, "nome");
    }

    public String getCodigoIata() {
        return codigoIata;
    }

    public String getCidade() {
        return cidade;
    }

    public String getNome() {
        return nome;
    }

    private static String validarCodigo(String codigo) {
        String normalizado = exigirTexto(codigo, "código IATA").toUpperCase(Locale.ROOT);
        if (!normalizado.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Código IATA deve possuir três letras.");
        }
        return normalizado;
    }

    private static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("O campo " + campo + " é obrigatório.");
        }
        return valor.trim();
    }

    @Override
    public String toString() {
        return cidade + " (" + codigoIata + ")";
    }

    @Override
    public boolean equals(Object outro) {
        return outro instanceof Aeroporto aeroporto && codigoIata.equals(aeroporto.codigoIata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoIata);
    }
}
