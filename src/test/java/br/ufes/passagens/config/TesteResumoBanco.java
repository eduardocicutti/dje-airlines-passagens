package br.ufes.passagens.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class TesteResumoBanco {
    private TesteResumoBanco() {
    }

    public static void main(String[] args) throws Exception {
        try (ConexaoMySQL banco = new ConexaoMySQL(ConfiguracaoBanco.carregar())) {
            Connection conexao = banco.Conectar();
            mostrarContagem(conexao, "passageiros");
            mostrarContagem(conexao, "pagamentos");
            mostrarContagem(conexao, "vendas");
            mostrarContagem(conexao, "etickets");
            mostrarUltimoETicket(conexao);
        }
    }

    private static void mostrarContagem(Connection conexao, String tabela) throws Exception {
        try (PreparedStatement comando = conexao.prepareStatement(
                "SELECT COUNT(*) total FROM " + tabela);
             ResultSet resultado = comando.executeQuery()) {
            resultado.next();
            System.out.println(tabela + ": " + resultado.getInt("total"));
        }
    }

    private static void mostrarUltimoETicket(Connection conexao) throws Exception {
        String sql = """
                SELECT v.id AS venda_id, e.numero, e.estado_geracao, e.estado_envio, e.caminho_pdf
                FROM vendas v
                JOIN etickets e ON e.venda_id = v.id
                ORDER BY v.id DESC
                LIMIT 1
                """;
        try (PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            if (resultado.next()) {
                System.out.println("ultima_venda: " + resultado.getLong("venda_id"));
                System.out.println("ultimo_eticket: " + resultado.getString("numero"));
                System.out.println("estado_geracao: " + resultado.getString("estado_geracao"));
                System.out.println("estado_envio: " + resultado.getString("estado_envio"));
                System.out.println("pdf: " + resultado.getString("caminho_pdf"));
            }
        }
    }
}
