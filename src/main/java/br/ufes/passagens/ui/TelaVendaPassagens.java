package br.ufes.passagens.ui;

import br.ufes.passagens.model.AssentoResumo;
import br.ufes.passagens.model.VendaConfirmada;
import br.ufes.passagens.model.Aeroporto;
import br.ufes.passagens.model.Pagamento;
import br.ufes.passagens.model.PagamentoCredito;
import br.ufes.passagens.model.PagamentoDebito;
import br.ufes.passagens.model.PagamentoDinheiro;
import br.ufes.passagens.model.Passageiro;
import br.ufes.passagens.model.Passagem;
import br.ufes.passagens.model.TipoPagamento;
import br.ufes.passagens.model.Trecho;
import br.ufes.passagens.model.Voo;
import br.ufes.passagens.repository.GerenciadorDados;
import br.ufes.passagens.service.CalculadoraPreco;
import br.ufes.passagens.service.CalendarioFeriadosFixos;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class TelaVendaPassagens extends JFrame {
    private final GerenciadorDados dados = new GerenciadorDados();
    private final CalculadoraPreco calculadora = new CalculadoraPreco(
            Clock.systemDefaultZone(), new CalendarioFeriadosFixos());
    private final CardLayout etapas = new CardLayout();
    private final JPanel painelEtapas = new JPanel(etapas);

    private final JComboBox<Aeroporto> comboOrigem = new JComboBox<>();
    private final JComboBox<Aeroporto> comboDestino = new JComboBox<>();
    private final DefaultListModel<Voo> modeloVoos = new DefaultListModel<>();
    private final JList<Voo> listaVoos = new JList<>(modeloVoos);
    private final JPanel painelAssentos = new JPanel(new GridLayout(10, 6, 6, 6));
    private final JTextField campoNome = new JTextField();
    private final JTextField campoDocumento = new JTextField();
    private final JTextField campoRetorno = new JTextField();
    private final JTextField campoValor = new JTextField();
    private final JRadioButton radioDinheiro = new JRadioButton("Dinheiro");
    private final JRadioButton radioCredito = new JRadioButton("Cartão de crédito");
    private final JRadioButton radioDebito = new JRadioButton("Cartão de débito");
    private final JTextField campoMatricula = new JTextField("1001");
    private final JTextField campoCartao = new JTextField();
    private final JTextField campoBandeira = new JTextField("VISA");
    private final JSpinner campoParcelas = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
    private final JLabel resumoFinal = new JLabel();

    private Voo vooSelecionado;
    private AssentoResumo assentoSelecionado;
    private Passagem passagemAtual;

    public TelaVendaPassagens() {
        super("DJE Airlines - Venda de Passagens");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(null);
        configurarRenderizadores();
        montarEtapas();
        carregarAeroportos();
        add(painelEtapas);
        etapas.show(painelEtapas, "origem");
    }

    private void configurarRenderizadores() {
        listaVoos.setCellRenderer((lista, voo, indice, selecionado, foco) -> {
            JLabel rotulo = new JLabel(descreverVoo(voo));
            rotulo.setOpaque(true);
            rotulo.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            rotulo.setBackground(selecionado ? new Color(0xCFE8FF) : Color.WHITE);
            return rotulo;
        });
        campoValor.setEditable(false);
        radioDinheiro.setSelected(true);
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(radioDinheiro);
        grupo.add(radioCredito);
        grupo.add(radioDebito);
        radioDinheiro.addActionListener(evento -> atualizarCamposPagamento());
        radioCredito.addActionListener(evento -> atualizarCamposPagamento());
        radioDebito.addActionListener(evento -> atualizarCamposPagamento());
        atualizarCamposPagamento();
    }

    private void montarEtapas() {
        painelEtapas.add(etapaOrigemDestino(), "origem");
        painelEtapas.add(etapaVoos(), "voos");
        painelEtapas.add(etapaAssentos(), "assentos");
        painelEtapas.add(etapaPagamento(), "pagamento");
        painelEtapas.add(etapaETicket(), "eticket");
    }

    private JPanel etapaOrigemDestino() {
        JPanel painel = painelBase("Etapa 1 - Origem e destino");
        JPanel formulario = new JPanel(new GridLayout(3, 2, 10, 10));
        formulario.add(new JLabel("Origem"));
        formulario.add(comboOrigem);
        formulario.add(new JLabel("Destino"));
        formulario.add(comboDestino);
        JButton proximo = new JButton("Buscar voos");
        proximo.addActionListener(evento -> buscarVoos());
        formulario.add(new JLabel());
        formulario.add(proximo);
        painel.add(formulario, BorderLayout.NORTH);
        return painel;
    }

    private JPanel etapaVoos() {
        JPanel painel = painelBase("Etapa 2 - Horário do voo");
        painel.add(new JScrollPane(listaVoos), BorderLayout.CENTER);
        JPanel botoes = barraBotoes("Voltar", () -> etapas.show(painelEtapas, "origem"),
                "Escolher voo", this::escolherVoo);
        painel.add(botoes, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel etapaAssentos() {
        JPanel painel = painelBase("Etapa 3 - Assento");
        painel.add(painelAssentos, BorderLayout.CENTER);
        painel.add(barraBotoes("Voltar", () -> etapas.show(painelEtapas, "voos"),
                "Confirmar assento", this::confirmarAssento), BorderLayout.SOUTH);
        return painel;
    }

    private JPanel etapaPagamento() {
        JPanel painel = painelBase("Etapa 4 - Pagamento");
        JPanel formulario = new JPanel(new GridLayout(13, 2, 8, 8));
        formulario.add(new JLabel("Nome do passageiro"));
        formulario.add(campoNome);
        formulario.add(new JLabel("Documento"));
        formulario.add(campoDocumento);
        formulario.add(new JLabel("Data de retorno opcional (AAAA-MM-DD)"));
        formulario.add(campoRetorno);
        formulario.add(new JLabel("Valor total"));
        formulario.add(campoValor);
        formulario.add(new JLabel("Forma de pagamento"));
        formulario.add(new JLabel());
        formulario.add(radioDinheiro);
        formulario.add(radioCredito);
        formulario.add(radioDebito);
        formulario.add(new JLabel());
        formulario.add(new JLabel("Matrícula funcionário (dinheiro)"));
        formulario.add(campoMatricula);
        formulario.add(new JLabel("Número do cartão"));
        formulario.add(campoCartao);
        formulario.add(new JLabel("Bandeira"));
        formulario.add(campoBandeira);
        formulario.add(new JLabel("Parcelas (crédito)"));
        formulario.add(campoParcelas);

        JButton recalcular = new JButton("Recalcular valor");
        recalcular.addActionListener(evento -> recalcularPassagem());
        JButton confirmar = new JButton("Confirmar pagamento");
        confirmar.addActionListener(evento -> confirmarPagamento());
        formulario.add(recalcular);
        formulario.add(confirmar);
        painel.add(formulario, BorderLayout.NORTH);
        painel.add(barraBotoes("Voltar", () -> etapas.show(painelEtapas, "assentos"),
                null, null), BorderLayout.SOUTH);
        return painel;
    }

    private JPanel etapaETicket() {
        JPanel painel = painelBase("Etapa 5 - E-ticket");
        resumoFinal.setFont(resumoFinal.getFont().deriveFont(Font.PLAIN, 16f));
        resumoFinal.setVerticalAlignment(JLabel.TOP);
        painel.add(resumoFinal, BorderLayout.CENTER);
        JButton novaVenda = new JButton("Nova venda");
        novaVenda.addActionListener(evento -> reiniciar());
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        barra.add(novaVenda);
        painel.add(barra, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel painelBase(String titulo) {
        JPanel painel = new JPanel(new BorderLayout(12, 12));
        painel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JLabel rotulo = new JLabel(titulo);
        rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 22f));
        painel.add(rotulo, BorderLayout.NORTH);
        return painel;
    }

    private JPanel barraBotoes(String textoVoltar, Runnable voltar, String textoAvancar, Runnable avancar) {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton botaoVoltar = new JButton(textoVoltar);
        botaoVoltar.addActionListener(evento -> voltar.run());
        barra.add(botaoVoltar);
        if (textoAvancar != null && avancar != null) {
            JButton botaoAvancar = new JButton(textoAvancar);
            botaoAvancar.addActionListener(evento -> avancar.run());
            barra.add(botaoAvancar);
        }
        return barra;
    }

    private void carregarAeroportos() {
        try {
            List<Aeroporto> aeroportos = dados.obterAeroportosPrincipais();
            DefaultComboBoxModel<Aeroporto> origem = new DefaultComboBoxModel<>();
            DefaultComboBoxModel<Aeroporto> destino = new DefaultComboBoxModel<>();
            aeroportos.forEach(aeroporto -> {
                origem.addElement(aeroporto);
                destino.addElement(aeroporto);
            });
            comboOrigem.setModel(origem);
            comboDestino.setModel(destino);
        } catch (SQLException e) {
            mostrarErro("Não foi possível carregar os aeroportos.", e);
        }
    }

    private void buscarVoos() {
        Aeroporto origem = (Aeroporto) comboOrigem.getSelectedItem();
        Aeroporto destino = (Aeroporto) comboDestino.getSelectedItem();
        if (origem == null || destino == null || origem.equals(destino)) {
            JOptionPane.showMessageDialog(this, "Selecione origem e destino diferentes.");
            return;
        }
        try {
            modeloVoos.clear();
            for (Voo voo : dados.obterVoosDisponiveis(origem.getCodigoIata(), destino.getCodigoIata())) {
                modeloVoos.addElement(voo);
            }
            if (modeloVoos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Não há voo cadastrado para esse trajeto.");
                return;
            }
            listaVoos.setSelectedIndex(0);
            etapas.show(painelEtapas, "voos");
        } catch (SQLException | IllegalArgumentException e) {
            mostrarErro("Falha ao buscar voos.", e);
        }
    }

    private void escolherVoo() {
        vooSelecionado = listaVoos.getSelectedValue();
        if (vooSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um voo.");
            return;
        }
        carregarAssentos();
        etapas.show(painelEtapas, "assentos");
    }

    private void carregarAssentos() {
        painelAssentos.removeAll();
        assentoSelecionado = null;
        try {
            for (AssentoResumo assento : dados.obterAssentos(vooSelecionado.getId())) {
                JToggleButton botao = new JToggleButton(assento.codigo());
                botao.setEnabled(assento.disponivel());
                botao.setBackground(assento.disponivel() ? new Color(0xE9F7EF) : new Color(0xF5CCCC));
                botao.addActionListener(evento -> selecionarAssento(botao, assento));
                painelAssentos.add(botao);
            }
            painelAssentos.revalidate();
            painelAssentos.repaint();
        } catch (SQLException e) {
            mostrarErro("Não foi possível carregar os assentos.", e);
        }
    }

    private void selecionarAssento(JToggleButton selecionado, AssentoResumo assento) {
        for (java.awt.Component componente : painelAssentos.getComponents()) {
            if (componente instanceof JToggleButton botao && botao != selecionado) {
                botao.setSelected(false);
            }
        }
        assentoSelecionado = selecionado.isSelected() ? assento : null;
    }

    private void confirmarAssento() {
        if (assentoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um assento disponível.");
            return;
        }
        recalcularPassagem();
        etapas.show(painelEtapas, "pagamento");
    }

    private void recalcularPassagem() {
        try {
            Passageiro passageiro = new Passageiro(null,
                    campoNome.getText().isBlank() ? "Passageiro" : campoNome.getText(),
                    campoDocumento.getText().isBlank() ? "TEMP" : campoDocumento.getText());
            passagemAtual = new Passagem(vooSelecionado, passageiro, assentoSelecionado.codigo(), lerRetorno());
            BigDecimal valor = calculadora.calcularValorTotal(passagemAtual);
            campoValor.setText("R$ " + valor);
        } catch (RuntimeException e) {
            mostrarErro("Não foi possível calcular o valor.", e);
        }
    }

    private void confirmarPagamento() {
        try {
            Passageiro passageiro = new Passageiro(null, campoNome.getText(), campoDocumento.getText());
            passagemAtual = new Passagem(vooSelecionado, passageiro, assentoSelecionado.codigo(), lerRetorno());
            calculadora.calcularValorTotal(passagemAtual);
            Pagamento pagamento = criarPagamento(passagemAtual.getValorTotal());
            VendaConfirmada venda = dados.confirmarVenda(passagemAtual, pagamento);
            mostrarETicket(venda);
            etapas.show(painelEtapas, "eticket");
        } catch (SQLException | RuntimeException e) {
            mostrarErro("Venda não concluída.", e);
        }
    }

    private LocalDate lerRetorno() {
        String texto = campoRetorno.getText().trim();
        if (texto.isEmpty()) return null;
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data de retorno deve estar no formato AAAA-MM-DD.");
        }
    }

    private Pagamento criarPagamento(BigDecimal valor) {
        TipoPagamento tipo = tipoSelecionado();
        if (tipo == TipoPagamento.DINHEIRO) {
            return new PagamentoDinheiro(valor, campoMatricula.getText());
        }
        if (tipo == TipoPagamento.CREDITO) {
            return new PagamentoCredito(valor, campoCartao.getText(), campoBandeira.getText(),
                    (Integer) campoParcelas.getValue());
        }
        return new PagamentoDebito(valor, campoCartao.getText(), campoBandeira.getText());
    }

    private TipoPagamento tipoSelecionado() {
        if (radioCredito.isSelected()) return TipoPagamento.CREDITO;
        if (radioDebito.isSelected()) return TipoPagamento.DEBITO;
        return TipoPagamento.DINHEIRO;
    }

    private void mostrarETicket(VendaConfirmada venda) {
        String caminho = venda.arquivoPdf() == null ? "PDF não gerado" : venda.arquivoPdf().getAbsolutePath();
        resumoFinal.setText("""
                <html>
                <h2>Venda confirmada</h2>
                <p><b>Venda:</b> %d</p>
                <p><b>E-ticket:</b> %s</p>
                <p><b>Valor:</b> R$ %s</p>
                <p><b>PDF:</b> %s</p>
                <p><b>Envio:</b> iniciado para output/servidor ou DJE_SERVER_DIR.</p>
                </html>
                """.formatted(venda.vendaId(), venda.numeroETicket(), venda.valorTotal(), caminho));
    }

    private void atualizarCamposPagamento() {
        boolean dinheiro = radioDinheiro.isSelected();
        campoMatricula.setEnabled(dinheiro);
        campoCartao.setEnabled(!dinheiro);
        campoBandeira.setEnabled(!dinheiro);
        campoParcelas.setEnabled(radioCredito.isSelected());
    }

    private void reiniciar() {
        vooSelecionado = null;
        assentoSelecionado = null;
        passagemAtual = null;
        modeloVoos.clear();
        campoNome.setText("");
        campoDocumento.setText("");
        campoRetorno.setText("");
        campoValor.setText("");
        campoCartao.setText("");
        etapas.show(painelEtapas, "origem");
    }

    private String descreverVoo(Voo voo) {
        return "%s | %s %s | %s -> %s | %d vagas | %s".formatted(
                voo.getCodigo(),
                voo.getData(),
                voo.getHorario(),
                voo.getOrigem().getCodigoIata(),
                voo.getDestino().getCodigoIata(),
                voo.getPoltronasVagas(),
                descreverTrechos(voo));
    }

    private String descreverTrechos(Voo voo) {
        StringBuilder texto = new StringBuilder();
        for (Trecho trecho : voo.getTrechos()) {
            if (!texto.isEmpty()) texto.append(" + ");
            texto.append(trecho.getOrigem().getCodigoIata())
                    .append("-")
                    .append(trecho.getDestino().getCodigoIata());
        }
        return texto.toString();
    }

    private void mostrarErro(String mensagem, Exception e) {
        JOptionPane.showMessageDialog(this, mensagem + "\n\n" + e.getMessage(),
                "DJE Airlines", JOptionPane.ERROR_MESSAGE);
    }

    public static void abrir() {
        SwingUtilities.invokeLater(() -> new TelaVendaPassagens().setVisible(true));
    }
}
