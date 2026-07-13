package br.ufes.passagens.ui;

import br.ufes.passagens.model.Aeroporto;
import br.ufes.passagens.model.AssentoResumo;
import br.ufes.passagens.model.Pagamento;
import br.ufes.passagens.model.PagamentoCredito;
import br.ufes.passagens.model.PagamentoDebito;
import br.ufes.passagens.model.PagamentoDinheiro;
import br.ufes.passagens.model.Passageiro;
import br.ufes.passagens.model.Passagem;
import br.ufes.passagens.model.TipoPagamento;
import br.ufes.passagens.model.Trecho;
import br.ufes.passagens.model.VendaConfirmada;
import br.ufes.passagens.model.Voo;
import br.ufes.passagens.repository.GerenciadorDados;
import br.ufes.passagens.service.CalculadoraPreco;
import br.ufes.passagens.service.CalendarioFeriadosFixos;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TelaVendaPassagens extends JFrame {
    private static final Color AZUL = new Color(0x1A365D);
    private static final Color AZUL_MEDIO = new Color(0x2B6CB0);
    private static final Color FUNDO = new Color(0xEDF2F7);
    private static final Color CARTAO = Color.WHITE;
    private static final Color TEXTO = new Color(0x2D3748);
    private static final Color CINZA = new Color(0x718596);
    private static final Color BORDA = new Color(0xCBD5E0);
    private static final Color VERDE = new Color(0xDFF7E8);
    private static final Color VERMELHO = new Color(0xF8D7DA);
    private static final Font FONTE = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONTE_NEGRITO = new Font("Segoe UI", Font.BOLD, 14);
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private static final Map<String, String> CIDADES = new HashMap<>();
    static {
        CIDADES.put("POA", "Porto Alegre");
        CIDADES.put("CGH", "São Paulo");
        CIDADES.put("GIG", "Rio de Janeiro");
        CIDADES.put("FOR", "Fortaleza");
        CIDADES.put("BSB", "Brasília");
        CIDADES.put("FLN", "Florianópolis");
        CIDADES.put("CNF", "Belo Horizonte");
        CIDADES.put("VIX", "Vitória");
    }

    private final GerenciadorDados dados = new GerenciadorDados();
    private final CalculadoraPreco calculadora = new CalculadoraPreco(
            Clock.systemDefaultZone(), new CalendarioFeriadosFixos());

    private final CardLayout etapas = new CardLayout();
    private final JPanel painelEtapas = new JPanel(etapas);
    private final PainelResumo painelResumo = new PainelResumo();

    private final JComboBox<Aeroporto> comboOrigem = new JComboBox<>();
    private final JComboBox<Aeroporto> comboDestino = new JComboBox<>();
    private final DefaultListModel<Voo> modeloVoos = new DefaultListModel<>();
    private final JList<Voo> listaVoos = new JList<>(modeloVoos);
    private final JPanel painelAssentos = new JPanel(new GridBagLayout());
    private final List<JToggleButton> botoesAssentos = new ArrayList<>();

    private final JTextField campoNome = new JTextField();
    private final JTextField campoDocumento = new JTextField();
    private final JCheckBox checkRetorno = new JCheckBox("Adicionar retorno");
    private final JSpinner campoRetorno = new JSpinner(new SpinnerDateModel(
            Date.from(LocalDate.now().plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant()),
            null, null, Calendar.DAY_OF_MONTH));
    private final JTextField campoValor = new JTextField();
    private final JRadioButton radioDinheiro = radioPagamento("Dinheiro");
    private final JRadioButton radioCredito = radioPagamento("Crédito");
    private final JRadioButton radioDebito = radioPagamento("Débito");
    private final JPanel painelDinheiro = painelCamposPagamento();
    private final JPanel painelCartao = painelCamposPagamento();
    private final JTextField campoMatricula = new JTextField("1001");
    private final JTextField campoCartao = new JTextField();
    private final JComboBox<String> campoBandeira = new JComboBox<>(new String[]{"Visa", "Mastercard", "Elo", "Amex"});
    private final JSpinner campoParcelas = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
    private final JLabel simulacaoParcelas = new JLabel("Selecione crédito para simular parcelamento.");
    private final JLabel resumoFinal = new JLabel();

    private Voo vooSelecionado;
    private AssentoResumo assentoSelecionado;
    private Passagem passagemAtual;

    public TelaVendaPassagens() {
        super("DJE Airlines");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setPreferredSize(new Dimension(1180, 760));
        aplicarEstilo();
        montarTela();
        carregarAeroportos();
        etapas.show(painelEtapas, "origem");
        pack();
        setLocationRelativeTo(null);
    }

    private void aplicarEstilo() {
        getContentPane().setBackground(FUNDO);
        listaVoos.setCellRenderer(renderizadorVoos());
        listaVoos.setFixedCellHeight(88);
        listaVoos.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        listaVoos.setBackground(FUNDO);
        campoValor.setEditable(false);
        campoValor.setFont(FONTE_NEGRITO);
        campoValor.setForeground(AZUL);

        comboOrigem.setRenderer((lista, valor, indice, selecionado, foco) ->
                rotuloLista(valor == null ? "" : formatarAeroporto(valor), selecionado));
        comboDestino.setRenderer((lista, valor, indice, selecionado, foco) ->
                rotuloLista(valor == null ? "" : formatarAeroporto(valor), selecionado));
        campoBandeira.setRenderer((lista, valor, indice, selecionado, foco) ->
                rotuloBandeira(valor == null ? "" : valor, selecionado));
        campoRetorno.setEditor(new JSpinner.DateEditor(campoRetorno, "dd/MM/yyyy"));
        campoRetorno.setEnabled(false);
        checkRetorno.setFont(FONTE_NEGRITO);
        checkRetorno.setForeground(AZUL);
        checkRetorno.setOpaque(false);
        checkRetorno.addActionListener(evento -> campoRetorno.setEnabled(checkRetorno.isSelected()));
        campoParcelas.addChangeListener(evento -> atualizarSimulacaoParcelas());

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(radioDinheiro);
        grupo.add(radioCredito);
        grupo.add(radioDebito);
        radioDinheiro.setSelected(true);
        radioDinheiro.addActionListener(evento -> atualizarCamposPagamento());
        radioCredito.addActionListener(evento -> atualizarCamposPagamento());
        radioDebito.addActionListener(evento -> atualizarCamposPagamento());
    }

    private void montarTela() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FUNDO);
        raiz.add(cabecalho(), BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(18, 18));
        corpo.setBackground(FUNDO);
        corpo.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        corpo.add(painelEtapas, BorderLayout.CENTER);
        corpo.add(painelResumo, BorderLayout.EAST);

        painelEtapas.setOpaque(false);
        painelEtapas.add(etapaOrigemDestino(), "origem");
        painelEtapas.add(etapaVoos(), "voos");
        painelEtapas.add(etapaAssentos(), "assentos");
        painelEtapas.add(etapaPagamento(), "pagamento");
        painelEtapas.add(etapaETicket(), "eticket");

        raiz.add(corpo, BorderLayout.CENTER);
        setContentPane(raiz);
    }

    private JPanel cabecalho() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(AZUL);
        topo.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JLabel marca = new JLabel("DJE AIRLINES");
        marca.setForeground(Color.WHITE);
        marca.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JLabel subtitulo = new JLabel("Venda de passagens aéreas");
        subtitulo.setForeground(new Color(0xBED5EF));
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel textos = new JPanel(new BorderLayout(12, 0));
        textos.setOpaque(false);
        textos.add(marca, BorderLayout.WEST);
        textos.add(subtitulo, BorderLayout.CENTER);

        JLabel ticket = new JLabel("E-TICKET");
        ticket.setForeground(Color.WHITE);
        ticket.setFont(new Font("Segoe UI", Font.BOLD, 18));

        topo.add(textos, BorderLayout.WEST);
        topo.add(ticket, BorderLayout.EAST);
        return topo;
    }

    private JPanel etapaOrigemDestino() {
        JPanel painel = painelBase("Escolha a rota", "Selecione origem e destino para buscar voos disponíveis.");
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        GridBagConstraints c = baseConstraints();

        adicionarCampo(formulario, c, 0, "Origem", comboOrigem);
        adicionarCampo(formulario, c, 1, "Destino", comboDestino);

        JButton buscar = botaoPrimario("Buscar voos");
        buscar.addActionListener(evento -> buscarVoos());
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        formulario.add(buscar, c);

        painel.add(formulario, BorderLayout.NORTH);
        return painel;
    }

    private JPanel etapaVoos() {
        JPanel painel = painelBase("Voo e horário", "Escolha um voo cadastrado no banco.");
        JScrollPane scroll = new JScrollPane(listaVoos);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(FUNDO);
        painel.add(scroll, BorderLayout.CENTER);
        painel.add(barraBotoes("Voltar", () -> etapas.show(painelEtapas, "origem"),
                "Escolher voo", this::escolherVoo), BorderLayout.SOUTH);
        return painel;
    }

    private JPanel etapaAssentos() {
        JPanel painel = painelBase("Assento", "Escolha no mapa do avião. Verdes estão livres; vermelhos já foram ocupados.");
        painelAssentos.setOpaque(false);
        JScrollPane scroll = scrollLimpo(painelAssentos);
        painel.add(scroll, BorderLayout.CENTER);
        painel.add(barraBotoes("Voltar", () -> etapas.show(painelEtapas, "voos"),
                "Confirmar assento", this::confirmarAssento), BorderLayout.SOUTH);
        return painel;
    }

    private JPanel etapaPagamento() {
        JPanel painel = painelBase("Passageiro e pagamento", "Informe os dados finais para confirmar a venda.");
        JPanel conteudo = new JPanel(new GridBagLayout());
        conteudo.setOpaque(false);
        GridBagConstraints c = baseConstraints();

        adicionarCampo(conteudo, c, 0, "Nome do passageiro", campoNome);
        adicionarCampo(conteudo, c, 1, "Documento", campoDocumento);
        adicionarCampo(conteudo, c, 2, "Retorno opcional", painelRetorno());
        adicionarCampo(conteudo, c, 3, "Valor total", campoValor);

        JPanel formas = new JPanel(new GridLayout(1, 3, 8, 8));
        formas.setOpaque(false);
        formas.add(radioDinheiro);
        formas.add(radioCredito);
        formas.add(radioDebito);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        conteudo.add(formas, c);

        montarPagamento();
        c.gridy = 5;
        conteudo.add(painelDinheiro, c);
        c.gridy = 6;
        conteudo.add(painelCartao, c);

        painel.add(scrollLimpo(conteudo), BorderLayout.CENTER);
        painel.add(barraPagamento(), BorderLayout.SOUTH);
        atualizarCamposPagamento();
        return painel;
    }

    private JPanel barraPagamento() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        barra.setOpaque(false);

        JButton voltar = botaoSecundario("Voltar");
        voltar.addActionListener(evento -> etapas.show(painelEtapas, "assentos"));

        JButton recalcular = botaoSecundario("Recalcular valor");
        recalcular.addActionListener(evento -> recalcularPassagem());

        JButton confirmar = botaoPrimario("Confirmar pagamento");
        confirmar.addActionListener(evento -> confirmarPagamento());

        barra.add(voltar);
        barra.add(recalcular);
        barra.add(confirmar);
        return barra;
    }

    private JPanel etapaETicket() {
        JPanel painel = painelBase("E-ticket", "Venda confirmada e PDF gerado.");
        resumoFinal.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        resumoFinal.setVerticalAlignment(JLabel.TOP);
        JPanel caixa = card();
        caixa.add(resumoFinal, BorderLayout.CENTER);
        painel.add(caixa, BorderLayout.CENTER);

        JButton novaVenda = botaoPrimario("Nova venda");
        novaVenda.addActionListener(evento -> reiniciar());
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        barra.setOpaque(false);
        barra.add(novaVenda);
        painel.add(barra, BorderLayout.SOUTH);
        return painel;
    }

    private void montarPagamento() {
        painelDinheiro.removeAll();
        painelCartao.removeAll();
        painelDinheiro.add(linhaCampo("Matrícula do funcionário", campoMatricula));
        painelDinheiro.add(avisoPagamento("Pagamento à vista no balcão."));
        painelCartao.add(linhaCampo("Número do cartão", campoCartao));
        painelCartao.add(linhaCampo("Bandeira", campoBandeira));
        painelCartao.add(linhaCampo("Parcelas", campoParcelas));
        painelCartao.add(simulacaoParcelas);
    }

    private JPanel painelBase(String titulo, String subtitulo) {
        JPanel externo = new JPanel(new BorderLayout(14, 14));
        externo.setOpaque(false);

        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        JLabel t = new JLabel(titulo);
        t.setFont(new Font("Segoe UI", Font.BOLD, 24));
        t.setForeground(AZUL);
        JLabel s = new JLabel(subtitulo);
        s.setFont(FONTE);
        s.setForeground(CINZA);
        cab.add(t, BorderLayout.NORTH);
        cab.add(s, BorderLayout.SOUTH);

        JPanel conteudo = card();
        conteudo.add(cab, BorderLayout.NORTH);
        externo.add(conteudo, BorderLayout.CENTER);
        return conteudo;
    }

    private JPanel card() {
        JPanel painel = new JPanel(new BorderLayout(14, 14));
        painel.setBackground(CARTAO);
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        return painel;
    }

    private JScrollPane scrollLimpo(Component conteudo) {
        JScrollPane scroll = new JScrollPane(conteudo);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(CARTAO);
        scroll.getViewport().setBackground(CARTAO);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JPanel barraBotoes(String textoVoltar, Runnable voltar, String textoAvancar, Runnable avancar) {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        barra.setOpaque(false);
        JButton botaoVoltar = botaoSecundario(textoVoltar);
        botaoVoltar.addActionListener(evento -> voltar.run());
        barra.add(botaoVoltar);
        if (textoAvancar != null && avancar != null) {
            JButton botaoAvancar = botaoPrimario(textoAvancar);
            botaoAvancar.addActionListener(evento -> avancar.run());
            barra.add(botaoAvancar);
        }
        return barra;
    }

    private void adicionarCampo(JPanel painel, GridBagConstraints c, int linha, String texto, Component campo) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setFont(FONTE_NEGRITO);
        rotulo.setForeground(TEXTO);
        c.gridx = 0;
        c.gridy = linha;
        c.gridwidth = 1;
        c.weightx = 0.25;
        c.fill = GridBagConstraints.HORIZONTAL;
        painel.add(rotulo, c);

        estilizarCampo(campo);
        c.gridx = 1;
        c.weightx = 0.75;
        painel.add(campo, c);
    }

    private JPanel linhaCampo(String texto, Component campo) {
        JPanel painel = new JPanel(new BorderLayout(8, 4));
        painel.setOpaque(false);
        JLabel rotulo = new JLabel(texto);
        rotulo.setFont(FONTE_NEGRITO);
        rotulo.setForeground(TEXTO);
        estilizarCampo(campo);
        painel.add(rotulo, BorderLayout.NORTH);
        painel.add(campo, BorderLayout.CENTER);
        return painel;
    }

    private JPanel painelRetorno() {
        JPanel painel = new JPanel(new BorderLayout(10, 0));
        painel.setOpaque(false);
        estilizarCampo(campoRetorno);
        painel.add(checkRetorno, BorderLayout.WEST);
        painel.add(campoRetorno, BorderLayout.CENTER);
        return painel;
    }

    private JLabel avisoPagamento(String texto) {
        JLabel aviso = new JLabel(texto);
        aviso.setFont(FONTE);
        aviso.setForeground(CINZA);
        aviso.setBorder(BorderFactory.createEmptyBorder(4, 2, 2, 2));
        return aviso;
    }

    private JPanel painelCamposPagamento() {
        JPanel painel = new JPanel(new GridLayout(0, 1, 8, 8));
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        return painel;
    }

    private GridBagConstraints baseConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 0, 8, 10);
        c.anchor = GridBagConstraints.WEST;
        return c;
    }

    private JButton botaoPrimario(String texto) {
        JButton botao = new JButton(texto);
        botao.setFont(FONTE_NEGRITO);
        botao.setForeground(Color.WHITE);
        botao.setBackground(AZUL_MEDIO);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return botao;
    }

    private JButton botaoSecundario(String texto) {
        JButton botao = new JButton(texto);
        botao.setFont(FONTE_NEGRITO);
        botao.setForeground(AZUL);
        botao.setBackground(new Color(0xE2E8F0));
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return botao;
    }

    private JRadioButton radioPagamento(String texto) {
        JRadioButton radio = new JRadioButton(texto);
        radio.setFont(FONTE_NEGRITO);
        radio.setForeground(AZUL);
        radio.setBackground(new Color(0xF7FAFC));
        radio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        radio.setFocusPainted(false);
        return radio;
    }

    private void estilizarCampo(Component campo) {
        campo.setFont(FONTE);
        campo.setForeground(TEXTO);
        if (campo instanceof JTextField texto) {
            texto.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDA),
                    BorderFactory.createEmptyBorder(9, 10, 9, 10)));
            texto.setBackground(Color.WHITE);
        } else if (campo instanceof JComboBox<?> combo) {
            combo.setBackground(Color.WHITE);
            combo.setBorder(BorderFactory.createLineBorder(BORDA));
        } else if (campo instanceof JSpinner spinner) {
            spinner.setBorder(BorderFactory.createLineBorder(BORDA));
        }
    }

    private ListCellRenderer<? super Voo> renderizadorVoos() {
        return (lista, voo, indice, selecionado, foco) -> {
            JPanel item = new JPanel(new BorderLayout(12, 4));
            item.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(selecionado ? AZUL_MEDIO : BORDA),
                    BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            item.setBackground(selecionado ? new Color(0xEBF4FF) : Color.WHITE);

            JLabel rota = new JLabel("%s  ->  %s".formatted(
                    voo.getOrigem().getCodigoIata(), voo.getDestino().getCodigoIata()));
            rota.setFont(new Font("Segoe UI", Font.BOLD, 22));
            rota.setForeground(AZUL);

            JLabel detalhes = new JLabel("%s | %s | %s | %d vagas".formatted(
                    voo.getCodigo(),
                    voo.getData().format(DATA_BR),
                    voo.getHorario(),
                    voo.getPoltronasVagas()));
            detalhes.setFont(FONTE);
            detalhes.setForeground(CINZA);

            JLabel trechos = new JLabel(descreverTrechos(voo));
            trechos.setFont(FONTE_NEGRITO);
            trechos.setForeground(AZUL_MEDIO);

            item.add(rota, BorderLayout.WEST);
            item.add(detalhes, BorderLayout.CENTER);
            item.add(trechos, BorderLayout.EAST);
            return item;
        };
    }

    private JLabel rotuloLista(String texto, boolean selecionado) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setOpaque(true);
        rotulo.setFont(FONTE);
        rotulo.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        rotulo.setForeground(TEXTO);
        rotulo.setBackground(selecionado ? new Color(0xEBF4FF) : Color.WHITE);
        return rotulo;
    }

    private JLabel rotuloBandeira(String texto, boolean selecionado) {
        JLabel rotulo = rotuloLista(texto, selecionado);
        rotulo.setIcon(new BandeiraIcon(texto));
        rotulo.setIconTextGap(10);
        return rotulo;
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
            painelResumo.atualizarRota(origem, destino, null, null);
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
        painelResumo.atualizarRota(vooSelecionado.getOrigem(), vooSelecionado.getDestino(), vooSelecionado, null);
        carregarAssentos();
        etapas.show(painelEtapas, "assentos");
    }

    private void carregarAssentos() {
        painelAssentos.removeAll();
        botoesAssentos.clear();
        assentoSelecionado = null;
        try {
            Map<String, AssentoResumo> mapa = new HashMap<>();
            for (AssentoResumo assento : dados.obterAssentos(vooSelecionado.getId())) {
                mapa.put(assento.codigo(), assento);
            }
            montarMapaAviao(mapa);
            painelAssentos.revalidate();
            painelAssentos.repaint();
        } catch (SQLException e) {
            mostrarErro("Não foi possível carregar os assentos.", e);
        }
    }

    private void montarMapaAviao(Map<String, AssentoResumo> assentos) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JLabel nariz = faixaAviao("DJE AIRLINES  •  CABINE");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 7;
        painelAssentos.add(nariz, c);

        String[] colunas = {"1", "2", "3", "", "4", "5", "6"};
        c.gridy = 1;
        c.gridwidth = 1;
        for (int i = 0; i < colunas.length; i++) {
            c.gridx = i;
            painelAssentos.add(rotuloColuna(colunas[i]), c);
        }

        String[] filas = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
        for (int linha = 0; linha < filas.length; linha++) {
            String fila = filas[linha];
            c.gridy = linha + 2;
            adicionarAssentoMapa(assentos.get(fila + "1"), c, 0);
            adicionarAssentoMapa(assentos.get(fila + "2"), c, 1);
            adicionarAssentoMapa(assentos.get(fila + "3"), c, 2);
            c.gridx = 3;
            painelAssentos.add(corredor(linha), c);
            adicionarAssentoMapa(assentos.get(fila + "4"), c, 4);
            adicionarAssentoMapa(assentos.get(fila + "5"), c, 5);
            adicionarAssentoMapa(assentos.get(fila + "6"), c, 6);
        }

        JLabel legenda = faixaAviao("Janela     Poltronas     Corredor     Poltronas     Janela");
        c.gridx = 0;
        c.gridy = 13;
        c.gridwidth = 7;
        painelAssentos.add(legenda, c);
    }

    private void adicionarAssentoMapa(AssentoResumo assento, GridBagConstraints c, int coluna) {
        c.gridx = coluna;
        c.gridwidth = 1;
        if (assento == null) {
            painelAssentos.add(new JLabel(), c);
            return;
        }
        JToggleButton botao = new JToggleButton(assento.codigo());
        botao.setPreferredSize(new Dimension(74, 42));
        botao.setFont(FONTE_NEGRITO);
        botao.setEnabled(assento.disponivel());
        botao.setBackground(assento.disponivel() ? VERDE : VERMELHO);
        botao.setForeground(TEXTO);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createLineBorder(assento.disponivel() ? new Color(0x68D391) : new Color(0xFC8181)));
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.addActionListener(evento -> selecionarAssento(botao, assento));
        botoesAssentos.add(botao);
        painelAssentos.add(botao, c);
    }

    private JLabel rotuloColuna(String texto) {
        JLabel rotulo = new JLabel(texto, JLabel.CENTER);
        rotulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rotulo.setForeground(CINZA);
        return rotulo;
    }

    private JLabel corredor(int linha) {
        JLabel label = new JLabel(linha == 4 ? "CORREDOR" : "│", JLabel.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, linha == 4 ? 10 : 16));
        label.setForeground(new Color(0xA0AEC0));
        return label;
    }

    private JLabel faixaAviao(String texto) {
        JLabel label = new JLabel(texto, JLabel.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(0xEBF4FF));
        label.setForeground(AZUL);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBEE3F8)),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));
        return label;
    }

    private void selecionarAssento(JToggleButton selecionado, AssentoResumo assento) {
        for (JToggleButton botao : botoesAssentos) {
            if (botao != selecionado) {
                botao.setSelected(false);
                if (botao.isEnabled()) {
                    botao.setBackground(VERDE);
                }
            }
        }
        assentoSelecionado = selecionado.isSelected() ? assento : null;
        selecionado.setBackground(assentoSelecionado == null ? VERDE : new Color(0x90CDF4));
        painelResumo.atualizarRota(vooSelecionado.getOrigem(), vooSelecionado.getDestino(), vooSelecionado, assentoSelecionado);
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
            campoValor.setText(MOEDA.format(valor));
            atualizarSimulacaoParcelas();
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
        if (!checkRetorno.isSelected()) return null;
        Date data = (Date) campoRetorno.getValue();
        return data.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Pagamento criarPagamento(BigDecimal valor) {
        TipoPagamento tipo = tipoSelecionado();
        if (tipo == TipoPagamento.DINHEIRO) {
            return new PagamentoDinheiro(valor, campoMatricula.getText());
        }
        if (tipo == TipoPagamento.CREDITO) {
            return new PagamentoCredito(valor, campoCartao.getText(), bandeiraSelecionada(),
                    (Integer) campoParcelas.getValue());
        }
        return new PagamentoDebito(valor, campoCartao.getText(), bandeiraSelecionada());
    }

    private String bandeiraSelecionada() {
        Object bandeira = campoBandeira.getSelectedItem();
        return bandeira == null ? "" : bandeira.toString().toUpperCase(Locale.ROOT);
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
                <h2 style='color:#1A365D'>Venda confirmada</h2>
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
        painelDinheiro.setVisible(dinheiro);
        painelCartao.setVisible(!dinheiro);
        campoParcelas.setEnabled(radioCredito.isSelected());
        radioDinheiro.setBackground(dinheiro ? new Color(0xEBF4FF) : new Color(0xF7FAFC));
        radioCredito.setBackground(radioCredito.isSelected() ? new Color(0xEBF4FF) : new Color(0xF7FAFC));
        radioDebito.setBackground(radioDebito.isSelected() ? new Color(0xEBF4FF) : new Color(0xF7FAFC));
        atualizarSimulacaoParcelas();
    }

    private void atualizarSimulacaoParcelas() {
        simulacaoParcelas.setFont(new Font("Segoe UI", Font.BOLD, 14));
        simulacaoParcelas.setForeground(AZUL);
        simulacaoParcelas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBEE3F8)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        simulacaoParcelas.setOpaque(true);
        simulacaoParcelas.setBackground(new Color(0xEBF8FF));

        BigDecimal valor = valorAtual();
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            simulacaoParcelas.setText("Simulação: calcule o valor para ver as parcelas.");
            return;
        }
        if (radioCredito.isSelected()) {
            int parcelas = (Integer) campoParcelas.getValue();
            BigDecimal valorParcela = valor.divide(BigDecimal.valueOf(parcelas), 2, RoundingMode.HALF_UP);
            simulacaoParcelas.setText("Simulação: %dx de %s sem juros".formatted(parcelas, MOEDA.format(valorParcela)));
        } else if (radioDebito.isSelected()) {
            simulacaoParcelas.setText("Simulação: débito à vista em %s".formatted(MOEDA.format(valor)));
        } else {
            simulacaoParcelas.setText("Simulação: pagamento à vista em %s".formatted(MOEDA.format(valor)));
        }
    }

    private BigDecimal valorAtual() {
        String texto = campoValor.getText()
                .replace("R$", "")
                .replace("\u00A0", "")
                .replace(" ", "")
                .replace(".", "")
                .replace(",", ".")
                .trim();
        if (texto.isBlank()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private void reiniciar() {
        vooSelecionado = null;
        assentoSelecionado = null;
        passagemAtual = null;
        modeloVoos.clear();
        campoNome.setText("");
        campoDocumento.setText("");
        checkRetorno.setSelected(false);
        campoRetorno.setEnabled(false);
        campoRetorno.setValue(Date.from(LocalDate.now().plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        campoValor.setText("");
        campoCartao.setText("");
        campoBandeira.setSelectedIndex(0);
        campoParcelas.setValue(1);
        atualizarSimulacaoParcelas();
        painelResumo.limpar();
        etapas.show(painelEtapas, "origem");
    }

    private String descreverTrechos(Voo voo) {
        StringBuilder texto = new StringBuilder();
        for (Trecho trecho : voo.getTrechos()) {
            if (!texto.isEmpty()) texto.append(" -> ");
            texto.append(trecho.getOrigem().getCodigoIata());
        }
        if (!voo.getTrechos().isEmpty()) {
            texto.append(" -> ").append(voo.getDestino().getCodigoIata());
        }
        return texto.toString();
    }

    private String formatarAeroporto(Aeroporto aeroporto) {
        return cidade(aeroporto) + " (" + aeroporto.getCodigoIata() + ")";
    }

    private String cidade(Aeroporto aeroporto) {
        return CIDADES.getOrDefault(aeroporto.getCodigoIata(), aeroporto.getCidade());
    }

    private void mostrarErro(String mensagem, Exception e) {
        JOptionPane.showMessageDialog(this, mensagem + "\n\n" + e.getMessage(),
                "DJE Airlines", JOptionPane.ERROR_MESSAGE);
    }

    public static void abrir() {
        SwingUtilities.invokeLater(() -> new TelaVendaPassagens().setVisible(true));
    }

    private final class BandeiraIcon implements Icon {
        private final String texto;

        private BandeiraIcon(String texto) {
            this.texto = texto;
        }

        @Override
        public int getIconWidth() {
            return 58;
        }

        @Override
        public int getIconHeight() {
            return 30;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            String nome = texto.toUpperCase(Locale.ROOT);
            Color fundo = switch (nome) {
                case "MASTERCARD" -> new Color(0x2D3748);
                case "ELO" -> new Color(0x111827);
                case "AMEX" -> new Color(0x2B6CB0);
                default -> new Color(0x1A365D);
            };
            g2.setColor(fundo);
            g2.fillRoundRect(x, y + 2, 54, 24, 8, 8);
            if ("MASTERCARD".equals(nome)) {
                g2.setColor(new Color(0xF56565));
                g2.fillOval(x + 9, y + 7, 17, 17);
                g2.setColor(new Color(0xECC94B));
                g2.fillOval(x + 23, y + 7, 17, 17);
            } else {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString(nome.length() > 4 ? nome.substring(0, 4) : nome, x + 10, y + 18);
            }
            g2.dispose();
        }
    }

    private final class PainelResumo extends JPanel {
        private String origem = "---";
        private String destino = "---";
        private String cidadeOrigem = "Origem";
        private String cidadeDestino = "Destino";
        private String voo = "VOO";
        private String dataHora = "--/--/---- --:--";
        private String assento = "--";

        private PainelResumo() {
            setPreferredSize(new Dimension(330, 0));
            setBackground(CARTAO);
            setBorder(BorderFactory.createLineBorder(BORDA));
        }

        private void limpar() {
            origem = "---";
            destino = "---";
            cidadeOrigem = "Origem";
            cidadeDestino = "Destino";
            voo = "VOO";
            dataHora = "--/--/---- --:--";
            assento = "--";
            repaint();
        }

        private void atualizarRota(Aeroporto origemAeroporto, Aeroporto destinoAeroporto,
                                   Voo vooSelecionado, AssentoResumo assentoResumo) {
            origem = origemAeroporto.getCodigoIata();
            destino = destinoAeroporto.getCodigoIata();
            cidadeOrigem = cidade(origemAeroporto);
            cidadeDestino = cidade(destinoAeroporto);
            if (vooSelecionado != null) {
                voo = vooSelecionado.getCodigo();
                dataHora = vooSelecionado.getData().format(DATA_BR) + " " + vooSelecionado.getHorario();
            }
            assento = assentoResumo == null ? "--" : assentoResumo.codigo();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();

            g2.setColor(AZUL);
            g2.fillRect(0, 0, w, 86);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2.drawString("DJE AIRLINES", 24, 36);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString("CARTÃO DE EMBARQUE", 24, 60);

            g2.setColor(AZUL);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 44));
            g2.drawString(origem, 34, 170);
            g2.drawString("→", 137, 170);
            g2.drawString(destino, 200, 170);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2.setColor(CINZA);
            g2.drawString(cidadeOrigem + " para " + cidadeDestino, 34, 196);

            desenharInfo(g2, 34, 250, "VOO", voo);
            desenharInfo(g2, 178, 250, "DATA/HORA", dataHora);
            desenharInfo(g2, 34, 320, "ASSENTO", assento);
            desenharInfo(g2, 178, 320, "STATUS", "EM VENDA");

            g2.setColor(new Color(0xE2E8F0));
            g2.fillRect(24, getHeight() - 96, w - 48, 64);
            g2.setColor(AZUL);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2.drawString(origem + " -> " + destino, 44, getHeight() - 58);
            g2.dispose();
        }

        private void desenharInfo(Graphics2D g2, int x, int y, String titulo, String valor) {
            g2.setColor(CINZA);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.drawString(titulo, x, y);
            g2.setColor(TEXTO);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2.drawString(valor, x, y + 22);
        }
    }
}
