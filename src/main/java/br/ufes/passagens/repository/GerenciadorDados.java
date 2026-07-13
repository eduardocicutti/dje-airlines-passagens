package br.ufes.passagens.repository;

import br.ufes.passagens.config.ConfiguracaoBanco;
import br.ufes.passagens.config.ConexaoMySQL;
import br.ufes.passagens.model.AssentoResumo;
import br.ufes.passagens.model.VendaConfirmada;
import br.ufes.passagens.model.Aeroporto;
import br.ufes.passagens.model.Pagamento;
import br.ufes.passagens.model.PagamentoCartao;
import br.ufes.passagens.model.PagamentoCredito;
import br.ufes.passagens.model.PagamentoDinheiro;
import br.ufes.passagens.model.Passageiro;
import br.ufes.passagens.model.Passagem;
import br.ufes.passagens.model.TipoPagamento;
import br.ufes.passagens.model.Trecho;
import br.ufes.passagens.model.Voo;
import br.ufes.passagens.util.GeradorPdf;
import br.ufes.passagens.util.GeradorTicket;
import br.ufes.passagens.util.TarefaEnvioServidor;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class GerenciadorDados {
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA_BR = DateTimeFormatter.ofPattern("HH:mm");
    private final ConfiguracaoBanco configuracao;

    public GerenciadorDados(ConfiguracaoBanco configuracao) {
        this.configuracao = configuracao;
    }

    public GerenciadorDados() {
        this(ConfiguracaoBanco.carregar());
    }

    public boolean testarConexao() throws SQLException {
        try (ConexaoMySQL banco = new ConexaoMySQL(configuracao)) {
            return banco.Conectar().isValid(5);
        }
    }

    public List<Aeroporto> obterAeroportosPrincipais() throws SQLException {
        String sql = """
                SELECT DISTINCT a.codigo_iata, a.cidade, a.nome
                FROM aeroportos a
                WHERE a.ativo = TRUE
                  AND (
                    EXISTS (
                      SELECT 1
                      FROM voos v
                      JOIN voo_trechos vt ON vt.voo_id = v.id
                      JOIN rotas r ON r.id = vt.rota_id
                      WHERE v.estado = 'DISPONIVEL'
                        AND v.data_voo >= CURDATE()
                        AND vt.ordem = (
                          SELECT MIN(vt2.ordem)
                          FROM voo_trechos vt2
                          WHERE vt2.voo_id = v.id
                        )
                        AND r.origem_id = a.id
                    )
                    OR EXISTS (
                      SELECT 1
                      FROM voos v
                      JOIN voo_trechos vt ON vt.voo_id = v.id
                      JOIN rotas r ON r.id = vt.rota_id
                      WHERE v.estado = 'DISPONIVEL'
                        AND v.data_voo >= CURDATE()
                        AND vt.ordem = (
                          SELECT MAX(vt2.ordem)
                          FROM voo_trechos vt2
                          WHERE vt2.voo_id = v.id
                        )
                        AND r.destino_id = a.id
                    )
                  )
                ORDER BY a.cidade
                """;
        List<Aeroporto> aeroportos = new ArrayList<>();
        try (ConexaoMySQL banco = new ConexaoMySQL(configuracao);
             PreparedStatement comando = banco.Conectar().prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) {
                aeroportos.add(new Aeroporto(
                        resultado.getString("codigo_iata"),
                        resultado.getString("cidade"),
                        resultado.getString("nome")));
            }
        }
        return List.copyOf(aeroportos);
    }

    public List<Voo> obterVoosDisponiveis(String origemIata, String destinoIata) throws SQLException {
        Map<Long, VooParcial> parciais = new LinkedHashMap<>();
        String sqlVoos = """
                SELECT v.id, v.codigo, v.data_voo, v.horario, v.portao, v.capacidade,
                       SUM(CASE WHEN a.estado = 'DISPONIVEL' THEN 1 ELSE 0 END) AS vagas
                FROM voos v
                JOIN assentos a ON a.voo_id = v.id
                WHERE v.estado = 'DISPONIVEL' AND v.data_voo >= CURDATE()
                GROUP BY v.id, v.codigo, v.data_voo, v.horario, v.portao, v.capacidade
                ORDER BY v.data_voo, v.horario
                """;
        try (ConexaoMySQL banco = new ConexaoMySQL(configuracao);
             PreparedStatement comando = banco.Conectar().prepareStatement(sqlVoos);
             ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) {
                long id = resultado.getLong("id");
                parciais.put(id, new VooParcial(
                        id,
                        resultado.getString("codigo"),
                        resultado.getDate("data_voo").toLocalDate(),
                        resultado.getTime("horario").toLocalTime(),
                        resultado.getString("portao"),
                        resultado.getInt("capacidade"),
                        resultado.getInt("vagas")));
            }

            for (VooParcial parcial : parciais.values()) {
                parcial.trechos.addAll(obterTrechosDoVoo(banco.Conectar(), parcial.id));
            }
        }

        String origem = normalizarIata(origemIata);
        String destino = normalizarIata(destinoIata);
        List<Voo> voos = new ArrayList<>();
        for (VooParcial parcial : parciais.values()) {
            if (parcial.trechos.isEmpty()) continue;
            Voo voo = parcial.paraVoo();
            if (voo.getOrigem().getCodigoIata().equals(origem)
                    && voo.getDestino().getCodigoIata().equals(destino)
                    && voo.getPoltronasVagas() > 0) {
                voos.add(voo);
            }
        }
        return List.copyOf(voos);
    }

    public List<AssentoResumo> obterAssentos(long vooId) throws SQLException {
        String sql = "SELECT id, codigo, estado FROM assentos WHERE voo_id = ? ORDER BY codigo";
        List<AssentoResumo> assentos = new ArrayList<>();
        try (ConexaoMySQL banco = new ConexaoMySQL(configuracao);
             PreparedStatement comando = banco.Conectar().prepareStatement(sql)) {
            comando.setLong(1, vooId);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    assentos.add(new AssentoResumo(
                            resultado.getLong("id"),
                            resultado.getString("codigo"),
                            resultado.getString("estado")));
                }
            }
        }
        return List.copyOf(assentos);
    }

    public VendaConfirmada confirmarVenda(Passagem passagem, Pagamento pagamento) throws SQLException {
        if (passagem.getVoo().getId() == null) {
            throw new IllegalArgumentException("Voo precisa estar persistido para venda.");
        }
        if (!pagamento.processarPagamento()) {
            throw new SQLException("Pagamento recusado.");
        }

        try (ConexaoMySQL banco = new ConexaoMySQL(configuracao)) {
            Connection conexao = banco.Conectar();
            boolean autoCommitInicial = conexao.getAutoCommit();
            conexao.setAutoCommit(false);
            try {
                long passageiroId = salvarPassageiro(conexao, passagem.getPassageiro());
                long assentoId = bloquearAssentoDisponivel(conexao, passagem.getVoo().getId(), passagem.getAssento());
                long pagamentoId = salvarPagamento(conexao, pagamento);
                long vendaId = salvarVenda(conexao, passageiroId, passagem.getVoo().getId(),
                        assentoId, pagamentoId, passagem.getValorTotal());
                ocuparAssento(conexao, assentoId);

                String numero = GeradorTicket.gerarNumeroETicket(
                        passagem.getPassageiro().getNome(),
                        passagem.getPassageiro().getDocumento(),
                        passagem.getVoo().getCodigo(),
                        passagem.getVoo().getData().toString(),
                        vendaId);
                long eticketId = salvarETicketPendente(conexao, vendaId, numero);

                conexao.commit();
                conexao.setAutoCommit(autoCommitInicial);

                File pdf = gerarPdf(passagem, numero);
                atualizarETicket(eticketId, pdf);
                iniciarEnvioETicket(eticketId, pdf);
                passagem.setNumeroETicket(numero);
                return new VendaConfirmada(vendaId, numero, pdf, passagem.getValorTotal());
            } catch (SQLException | RuntimeException e) {
                conexao.rollback();
                conexao.setAutoCommit(autoCommitInicial);
                throw e;
            }
        }
    }

    public boolean isAssentoOcupado(long vooId, String codigoAssento) throws SQLException {
        String sql = "SELECT estado FROM assentos WHERE voo_id = ? AND codigo = ?";
        try (ConexaoMySQL banco = new ConexaoMySQL(configuracao);
             PreparedStatement comando = banco.Conectar().prepareStatement(sql)) {
            comando.setLong(1, vooId);
            comando.setString(2, codigoAssento.trim().toUpperCase());
            try (ResultSet resultado = comando.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("Assento não cadastrado para o voo informado.");
                }
                return !"DISPONIVEL".equals(resultado.getString("estado"));
            }
        }
    }

    private List<Trecho> obterTrechosDoVoo(Connection conexao, long vooId) throws SQLException {
        String sql = """
                SELECT oo.codigo_iata AS origem_iata, oo.cidade AS origem_cidade, oo.nome AS origem_nome,
                       dd.codigo_iata AS destino_iata, dd.cidade AS destino_cidade, dd.nome AS destino_nome,
                       r.distancia_milhas
                FROM voo_trechos vt
                JOIN rotas r ON r.id = vt.rota_id
                JOIN aeroportos oo ON oo.id = r.origem_id
                JOIN aeroportos dd ON dd.id = r.destino_id
                WHERE vt.voo_id = ?
                ORDER BY vt.ordem
                """;
        List<Trecho> trechos = new ArrayList<>();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, vooId);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    Aeroporto origem = new Aeroporto(
                            resultado.getString("origem_iata"),
                            resultado.getString("origem_cidade"),
                            resultado.getString("origem_nome"));
                    Aeroporto destino = new Aeroporto(
                            resultado.getString("destino_iata"),
                            resultado.getString("destino_cidade"),
                            resultado.getString("destino_nome"));
                    trechos.add(new Trecho(origem, destino, resultado.getInt("distancia_milhas")));
                }
            }
        }
        return trechos;
    }

    private long salvarPassageiro(Connection conexao, Passageiro passageiro) throws SQLException {
        String sql = """
                INSERT INTO passageiros (nome, documento)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE nome = VALUES(nome), id = LAST_INSERT_ID(id)
                """;
        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, passageiro.getNome());
            comando.setString(2, passageiro.getDocumento());
            comando.executeUpdate();
            return chaveGerada(comando);
        }
    }

    private long bloquearAssentoDisponivel(Connection conexao, long vooId, String codigoAssento) throws SQLException {
        String sql = "SELECT id, estado FROM assentos WHERE voo_id = ? AND codigo = ? FOR UPDATE";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, vooId);
            comando.setString(2, codigoAssento);
            try (ResultSet resultado = comando.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("Assento não encontrado para este voo.");
                }
                if (!"DISPONIVEL".equals(resultado.getString("estado"))) {
                    throw new SQLException("Assento já está ocupado ou reservado.");
                }
                return resultado.getLong("id");
            }
        }
    }

    private long salvarPagamento(Connection conexao, Pagamento pagamento) throws SQLException {
        String sql = """
                INSERT INTO pagamentos
                (tipo, valor, estado, funcionario_id, cartao_bandeira, cartao_final, parcelas, processado_em)
                VALUES (?, ?, 'APROVADO', ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
        Long funcionarioId = null;
        String bandeira = null;
        String cartaoFinal = null;
        Integer parcelas = null;

        if (pagamento instanceof PagamentoDinheiro dinheiro) {
            funcionarioId = obterFuncionarioId(conexao, dinheiro.getMatriculaFuncionario());
        } else if (pagamento instanceof PagamentoCartao cartao) {
            bandeira = cartao.getBandeira();
            cartaoFinal = cartao.getUltimosQuatroDigitos();
            if (pagamento instanceof PagamentoCredito credito) {
                parcelas = credito.getParcelas();
            }
        }

        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, pagamento.getTipo().name());
            comando.setBigDecimal(2, pagamento.getValorTotal());
            setLongOuNull(comando, 3, funcionarioId);
            comando.setString(4, bandeira);
            comando.setString(5, cartaoFinal);
            if (parcelas == null) comando.setNull(6, java.sql.Types.TINYINT);
            else comando.setInt(6, parcelas);
            comando.executeUpdate();
            return chaveGerada(comando);
        }
    }

    private Long obterFuncionarioId(Connection conexao, String matricula) throws SQLException {
        String sql = "SELECT id FROM funcionarios WHERE matricula = ? AND ativo = TRUE";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, matricula);
            try (ResultSet resultado = comando.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("Funcionário não encontrado ou inativo.");
                }
                return resultado.getLong("id");
            }
        }
    }

    private long salvarVenda(Connection conexao, long passageiroId, long vooId, long assentoId,
                             long pagamentoId, BigDecimal valorTotal) throws SQLException {
        String sql = """
                INSERT INTO vendas
                (passageiro_id, voo_id, assento_id, pagamento_id, valor_total)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setLong(1, passageiroId);
            comando.setLong(2, vooId);
            comando.setLong(3, assentoId);
            comando.setLong(4, pagamentoId);
            comando.setBigDecimal(5, valorTotal);
            comando.executeUpdate();
            return chaveGerada(comando);
        }
    }

    private void ocuparAssento(Connection conexao, long assentoId) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(
                "UPDATE assentos SET estado = 'OCUPADO', reservado_ate = NULL WHERE id = ?")) {
            comando.setLong(1, assentoId);
            comando.executeUpdate();
        }
    }

    private long salvarETicketPendente(Connection conexao, long vendaId, String numero) throws SQLException {
        String sql = "INSERT INTO etickets (venda_id, numero) VALUES (?, ?)";
        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setLong(1, vendaId);
            comando.setString(2, numero);
            comando.executeUpdate();
            return chaveGerada(comando);
        }
    }

    private File gerarPdf(Passagem passagem, String numero) {
        Voo voo = passagem.getVoo();
        return GeradorPdf.gerarETicket(
                passagem.getPassageiro().getNome(),
                voo.getCodigo(),
                formatarAeroporto(voo.getOrigem()),
                formatarAeroporto(voo.getDestino()),
                voo.getData().format(DATA_BR),
                voo.getHorario().format(HORA_BR),
                numero,
                voo.getPortao(),
                passagem.getAssento(),
                descreverItinerario(voo),
                passagem.getValorTotal());
    }

    private void atualizarETicket(long eticketId, File pdf) throws SQLException {
        String sql = """
                UPDATE etickets
                SET caminho_pdf = ?, estado_geracao = ?, gerado_em = ?, mensagem_erro = ?
                WHERE id = ?
                """;
        try (ConexaoMySQL banco = new ConexaoMySQL(configuracao);
             PreparedStatement comando = banco.Conectar().prepareStatement(sql)) {
            if (pdf == null) {
                comando.setString(1, null);
                comando.setString(2, "FALHA");
                comando.setTimestamp(3, null);
                comando.setString(4, "Falha ao gerar PDF.");
            } else {
                comando.setString(1, pdf.getAbsolutePath());
                comando.setString(2, "GERADO");
                comando.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                comando.setString(4, null);
            }
            comando.setLong(5, eticketId);
            comando.executeUpdate();
        }
    }

    private void iniciarEnvioETicket(long eticketId, File pdf) {
        if (pdf == null) {
            return;
        }
        Thread envio = new Thread(new TarefaEnvioServidor(pdf, resultado -> {
            try {
                atualizarEnvioETicket(eticketId, resultado);
            } catch (SQLException e) {
                System.err.println("Falha ao atualizar envio do e-ticket: " + e.getMessage());
            }
        }), "envio-eticket-" + eticketId);
        envio.start();
    }

    private void atualizarEnvioETicket(long eticketId, TarefaEnvioServidor.ResultadoEnvio resultado)
            throws SQLException {
        String sql = """
                UPDATE etickets
                SET estado_envio = ?, enviado_em = ?, mensagem_erro = ?
                WHERE id = ?
                """;
        try (ConexaoMySQL banco = new ConexaoMySQL(configuracao);
             PreparedStatement comando = banco.Conectar().prepareStatement(sql)) {
            if (resultado.enviado()) {
                comando.setString(1, "ENVIADO");
                comando.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                comando.setString(3, null);
            } else {
                comando.setString(1, "FALHA");
                comando.setTimestamp(2, null);
                comando.setString(3, resultado.erro());
            }
            comando.setLong(4, eticketId);
            comando.executeUpdate();
        }
    }

    private long chaveGerada(PreparedStatement comando) throws SQLException {
        try (ResultSet chaves = comando.getGeneratedKeys()) {
            if (chaves.next()) return chaves.getLong(1);
        }
        throw new SQLException("Banco não retornou a chave gerada.");
    }

    private void setLongOuNull(PreparedStatement comando, int indice, Long valor) throws SQLException {
        if (valor == null) comando.setNull(indice, java.sql.Types.BIGINT);
        else comando.setLong(indice, valor);
    }

    private String normalizarIata(String codigo) {
        if (codigo == null || !codigo.trim().toUpperCase().matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Código IATA inválido.");
        }
        return codigo.trim().toUpperCase();
    }

    private String formatarAeroporto(Aeroporto aeroporto) {
        return aeroporto.getCidade() + " (" + aeroporto.getCodigoIata() + ")";
    }

    private String descreverItinerario(Voo voo) {
        if (voo.getTrechos().isEmpty()) {
            return voo.getOrigem().getCodigoIata() + " -> " + voo.getDestino().getCodigoIata();
        }
        String inicio = voo.getTrechos().get(0).getOrigem().getCodigoIata();
        String destinos = voo.getTrechos().stream()
                .map(trecho -> trecho.getDestino().getCodigoIata())
                .collect(Collectors.joining(" -> "));
        return inicio + " -> " + destinos;
    }

    private static final class VooParcial {
        private final long id;
        private final String codigo;
        private final java.time.LocalDate data;
        private final java.time.LocalTime horario;
        private final String portao;
        private final int capacidade;
        private final int vagas;
        private final List<Trecho> trechos = new ArrayList<>();

        private VooParcial(long id, String codigo, java.time.LocalDate data,
                           java.time.LocalTime horario, String portao, int capacidade, int vagas) {
            this.id = id;
            this.codigo = codigo;
            this.data = data;
            this.horario = horario;
            this.portao = portao;
            this.capacidade = capacidade;
            this.vagas = vagas;
        }

        private Voo paraVoo() {
            return new Voo(id, codigo, data, horario, portao, capacidade, vagas, trechos);
        }
    }
}
