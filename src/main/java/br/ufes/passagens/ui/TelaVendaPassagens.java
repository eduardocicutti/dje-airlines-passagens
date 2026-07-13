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
import javax.swing.ListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
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
import java.awt.Image;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
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
    private static final String LOGO = "/imagens/logo-dje.png";

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
    private final Image logo = carregarLogo();

    private final JComboBox<Aeroporto> comboOrigem = new JComboBox<>();
    private final JComboBox<Aeroporto> comboDestino = new JComboBox<>();
    private final DefaultListModel<Voo> modeloVoos = new DefaultListModel<>();
    private final JList<Voo> listaVoos = new JList<>(modeloVoos);
    private final JPanel painelAssentos = new JPanel(new GridLayout(10, 6, 8, 8));

    private final JTextField campoNome = new JTextField();
    private final JTextField campoDocumento = new JTextField();
    private final JTextField campoRetorno = new JTextField();
    private final JTextField campoValor = new JTextField();
    private final JRadioButton radioDinheiro = radioPagamento("Dinheiro");
    private final JRadioButton radioCredito = radioPagamento("Crédito");
    private final JRadioButton radioDebito = radioPagamento("Débito");
    private final JPanel painelDinheiro = painelCamposPagamento();
    private final JPanel painelCartao = painelCamposPagamento();
    private final JTextField campoMatricula = new JTextField("1001");
    private final JTextField campoCartao = new JTextField();
    private final JTextField campoBandeira = new JTextField("VISA");
    private final JSpinner campoParcelas = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
    private final JLabel resumoFinal = new JLabel();

    private Voo vooSelecionado;
    private AssentoResumo assentoSelecionado;
    private Passagem passagemAtual;

    public TelaVendaPassagens() {
        super("DJE Airlines");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);
        aplicarEstilo();
        montarTela();
        carregarAeroportos();
        etapas.show(painelEtapas, "origem");
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

        JLabel marca;
        if (logo == null) {
            marca = new JLabel("DJE AIRLINES");
            marca.setForeground(Color.WHITE);
            marca.setFont(new Font("Segoe UI", Font.BOLD, 28));
        } else {
            Image ajustada = logo.getScaledInstance(188, 76, Image.SCALE_SMOOTH);
            marca = new JLabel(new ImageIcon(ajustada));
        }

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

    private Image carregarLogo() {
        java.net.URL url = TelaVendaPassagens.class.getResource(LOGO);
        return url == null ? null : new ImageIcon(url).getImage();
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
        JPanel painel = painelBase("Assento", "Assentos verdes estão disponíveis; vermelhos já foram ocupados.");
        painelAssentos.setOpaque(false);
        painel.add(painelAssentos, BorderLayout.CENTER);
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
        adicionarCampo(conteudo, c, 2, "Retorno opcional (AAAA-MM-DD)", campoRetorno);
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

        JButton recalcular = botaoSecundario("Recalcular valor");
        recalcular.addActionListener(evento -> recalcularPassagem());
        JButton confirmar = botaoPrimario("Confirmar pagamento");
        confirmar.addActionListener(evento -> confirmarPagamento());
        JPanel acoes = new JPanel(new GridLayout(1, 2, 8, 8));
        acoes.setOpaque(false);
        acoes.add(recalcular);
        acoes.add(confirmar);
        c.gridy = 7;
        conteudo.add(acoes, c);

        painel.add(conteudo, BorderLayout.NORTH);
        painel.add(barraBotoes("Voltar", () -> etapas.show(painelEtapas, "assentos"),
                null, null), BorderLayout.SOUTH);
        atualizarCamposPagamento();
        return painel;
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
        painelCartao.add(linhaCampo("Número do cartão", campoCartao));
        painelCartao.add(linhaCampo("Bandeira", campoBandeira));
        painelCartao.add(linhaCampo("Parcelas", campoParcelas));
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
        assentoSelecionado = null;
        try {
            for (AssentoResumo assento : dados.obterAssentos(vooSelecionado.getId())) {
                JToggleButton botao = new JToggleButton(assento.codigo());
                botao.setFont(FONTE_NEGRITO);
                botao.setEnabled(assento.disponivel());
                botao.setBackground(assento.disponivel() ? VERDE : VERMELHO);
                botao.setForeground(TEXTO);
                botao.setFocusPainted(false);
                botao.setBorder(BorderFactory.createLineBorder(assento.disponivel() ? new Color(0x9AE6B4) : new Color(0xFEB2B2)));
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
        for (Component componente : painelAssentos.getComponents()) {
            if (componente instanceof JToggleButton botao && botao != selecionado) {
                botao.setSelected(false);
                botao.setBackground(VERDE);
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
            if (logo == null) {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                g2.drawString("DJE AIRLINES", 24, 38);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.drawString("CARTÃO DE EMBARQUE", 24, 60);
            } else {
                g2.drawImage(logo, 24, 8, 142, 70, null);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.drawString("CARTÃO DE EMBARQUE", 178, 48);
            }

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
