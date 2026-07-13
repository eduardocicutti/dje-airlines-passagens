package br.ufes.passagens.config;

public final class TesteConexao {
    private TesteConexao() { }

    public static void main(String[] args) {
        ConfiguracaoBanco configuracao = ConfiguracaoBanco.carregar();
        try (ConexaoMySQL banco = new ConexaoMySQL(configuracao)) {
            if (banco.Conectar().isValid(5)) {
                System.out.println("Conexão com o MySQL estabelecida com sucesso.");
            } else {
                System.err.println("O MySQL não confirmou a conexão.");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Falha ao conectar ao MySQL: " + e.getMessage());
            System.exit(1);
        }
    }
}
