package br.ufes.passagens.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public record ConfiguracaoBanco(String url, String usuario, String senha) {
    public static ConfiguracaoBanco carregar() {
        Properties arquivo = new Properties();
        try (InputStream entrada = ConfiguracaoBanco.class.getResourceAsStream("/application.properties")) {
            if (entrada != null) arquivo.load(entrada);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler application.properties.", e);
        }
        String url = obter("DJE_DB_URL", "db.url", arquivo,
                "jdbc:mysql://localhost:3306/dje_airlines?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true");
        String usuario = obter("DJE_DB_USER", "db.user", arquivo, "root");
        String senha = obter("DJE_DB_PASSWORD", "db.password", arquivo, "");
        return new ConfiguracaoBanco(url, usuario, senha);
    }

    private static String obter(String ambiente, String propriedade, Properties arquivo, String padrao) {
        String valorAmbiente = System.getenv(ambiente);
        if (valorAmbiente != null && !valorAmbiente.isBlank()) return valorAmbiente;
        return arquivo.getProperty(propriedade, padrao).trim();
    }
}
