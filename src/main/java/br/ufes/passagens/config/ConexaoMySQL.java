package br.ufes.passagens.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class ConexaoMySQL implements AutoCloseable {
    private final ConfiguracaoBanco configuracao;
    private Connection conexao;

    public ConexaoMySQL(ConfiguracaoBanco configuracao) {
        this.configuracao = configuracao;
    }

    public Connection Conectar() throws SQLException {
        if (conexao == null || conexao.isClosed()) {
            conexao = DriverManager.getConnection(
                    configuracao.url(), configuracao.usuario(), configuracao.senha());
        }
        return conexao;
    }

    public int InserirDadosNoMySQL(String sql, Object... parametros) throws SQLException {
        if (sql == null || sql.isBlank()) throw new IllegalArgumentException("SQL obrigatório.");
        try (PreparedStatement comando = Conectar().prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                comando.setObject(i + 1, parametros[i]);
            }
            return comando.executeUpdate();
        }
    }

    public void Desconectar() throws SQLException {
        if (conexao != null && !conexao.isClosed()) conexao.close();
        conexao = null;
    }

    @Override
    public void close() throws SQLException {
        Desconectar();
    }
}
