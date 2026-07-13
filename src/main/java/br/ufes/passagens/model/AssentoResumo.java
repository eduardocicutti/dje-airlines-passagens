package br.ufes.passagens.model;

public record AssentoResumo(long id, String codigo, String estado) {
    public boolean disponivel() {
        return "DISPONIVEL".equals(estado);
    }

    @Override
    public String toString() {
        return codigo;
    }
}
