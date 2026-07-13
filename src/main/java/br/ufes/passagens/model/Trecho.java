package br.ufes.passagens.model;

import java.util.Objects;

public final class Trecho {
    private final Aeroporto origem;
    private final Aeroporto destino;
    private final int distanciaMilhas;

    public Trecho(Aeroporto origem, Aeroporto destino, int distanciaMilhas) {
        this.origem = Objects.requireNonNull(origem, "Origem obrigatória.");
        this.destino = Objects.requireNonNull(destino, "Destino obrigatório.");
        if (origem.equals(destino)) {
            throw new IllegalArgumentException("Origem e destino não podem ser iguais.");
        }
        if (distanciaMilhas <= 0) {
            throw new IllegalArgumentException("Distância deve ser positiva.");
        }
        this.distanciaMilhas = distanciaMilhas;
    }

    public Aeroporto getOrigem() {
        return origem;
    }

    public Aeroporto getDestino() {
        return destino;
    }

    public int getDistanciaMilhas() {
        return distanciaMilhas;
    }
}
