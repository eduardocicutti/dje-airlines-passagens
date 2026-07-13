package br.ufes.passagens.model;

public final class Passageiro {
    private final Long id;
    private final String nome;
    private final String documento;

    public Passageiro(Long id, String nome, String documento) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do passageiro é obrigatório.");
        }
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("Documento do passageiro é obrigatório.");
        }
        this.id = id;
        this.nome = nome.trim();
        this.documento = documento.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDocumento() { return documento; }
}
