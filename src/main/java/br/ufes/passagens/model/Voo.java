package br.ufes.passagens.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public final class Voo {
    private final Long id;
    private final String codigo;
    private final LocalDate data;
    private final LocalTime horario;
    private final String portao;
    private final int capacidade;
    private final int poltronasVagas;
    private final List<Trecho> trechos;

    public Voo(Long id, String codigo, LocalDate data, LocalTime horario, String portao,
               int capacidade, int poltronasVagas, List<Trecho> trechos) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código do voo é obrigatório.");
        }
        if (capacidade <= 0 || poltronasVagas < 0 || poltronasVagas > capacidade) {
            throw new IllegalArgumentException("Capacidade ou quantidade de vagas inválida.");
        }
        if (trechos == null || trechos.isEmpty()) {
            throw new IllegalArgumentException("O voo precisa de ao menos um trecho.");
        }
        this.id = id;
        this.codigo = codigo.trim().toUpperCase();
        this.data = Objects.requireNonNull(data, "Data obrigatória.");
        this.horario = Objects.requireNonNull(horario, "Horário obrigatório.");
        this.portao = Objects.requireNonNull(portao, "Portão obrigatório.").trim().toUpperCase();
        this.capacidade = capacidade;
        this.poltronasVagas = poltronasVagas;
        this.trechos = List.copyOf(trechos);
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public LocalDate getData() { return data; }
    public LocalTime getHorario() { return horario; }
    public String getPortao() { return portao; }
    public int getCapacidade() { return capacidade; }
    public int getPoltronasVagas() { return poltronasVagas; }
    public List<Trecho> getTrechos() { return trechos; }
    public Aeroporto getOrigem() { return trechos.get(0).getOrigem(); }
    public Aeroporto getDestino() { return trechos.get(trechos.size() - 1).getDestino(); }
}
