package br.ufes.passagens.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class GeradorPdf {

    private static final Color COR_AZUL_MARINHO    = new Color(0x1A, 0x36, 0x5D);
    private static final Color COR_AZUL_MEDIO      = new Color(0x2B, 0x6C, 0xB0);
    private static final Color COR_CINZA_CLARO     = new Color(0xED, 0xF2, 0xF7);
    private static final Color COR_GRAFITE         = new Color(0x2D, 0x37, 0x48);
    private static final Color COR_CINZA_LABEL     = new Color(0x71, 0x85, 0x96);
    private static final Color COR_BRANCO          = Color.WHITE;
    private static final Color COR_LINHA_TRACEJADA = new Color(0xCB, 0xD5, 0xE0);
    private static final String FONTE_NORMAL = "C:/Windows/Fonts/arial.ttf";
    private static final String FONTE_NEGRITO = "C:/Windows/Fonts/arialbd.ttf";
    private static final String LOGO = "/imagens/logo-dje.png";
    private static final Map<String, String> CIDADES = Map.of(
            "POA", "Porto Alegre",
            "CGH", "São Paulo",
            "GIG", "Rio de Janeiro",
            "FOR", "Fortaleza",
            "BSB", "Brasília",
            "FLN", "Florianópolis",
            "CNF", "Belo Horizonte",
            "VIX", "Vitória");

    public static File gerarETicket(
            String nomePassageiro,
            String codigoVoo,
            String origem,
            String destino,
            String dataVoo,
            String horarioVoo,
            String numTicket,
            String portaoEmbarque
    ) {
        return gerarETicket(nomePassageiro, codigoVoo, origem, destino, dataVoo, horarioVoo,
                numTicket, portaoEmbarque, "N/D", origem + " -> " + destino, null);
    }

    public static File gerarETicket(
            String nomePassageiro,
            String codigoVoo,
            String origem,
            String destino,
            String dataVoo,
            String horarioVoo,
            String numTicket,
            String portaoEmbarque,
            String assento,
            String itinerario,
            BigDecimal valorTotal
    ) {
        String nomeArquivo = "eticket_" + numTicket + ".pdf";
        File arquivoPdf = Path.of("output", "etickets", nomeArquivo).toFile();

        Document documento = new Document(PageSize.A5.rotate(), 30, 30, 24, 24);

        try {
            Files.createDirectories(arquivoPdf.toPath().getParent());
            PdfWriter.getInstance(documento, new FileOutputStream(arquivoPdf));
            documento.open();

            String siglaOrigem  = extrairSigla(origem);
            String siglaDestino = extrairSigla(destino);
            String cidadeOrigem = CIDADES.getOrDefault(siglaOrigem, extrairCidade(origem));
            String cidadeDestino = CIDADES.getOrDefault(siglaDestino, extrairCidade(destino));

            PdfPTable cabecalho = new PdfPTable(new float[]{3f, 2f});
            cabecalho.setWidthPercentage(100);
            PdfPCell marca = celulaLogo();
            marca.setPadding(12);
            PdfPCell ticket = criarCelula("E-TICKET\n" + numTicket, 12, true, COR_BRANCO, COR_AZUL_MARINHO);
            ticket.setHorizontalAlignment(Element.ALIGN_RIGHT);
            ticket.setPadding(12);
            cabecalho.addCell(marca);
            cabecalho.addCell(ticket);
            documento.add(cabecalho);

            Paragraph rota = new Paragraph(
                    siglaOrigem + "  ->  " + siglaDestino,
                    fonte(34, true, COR_AZUL_MARINHO));
            rota.setAlignment(Element.ALIGN_CENTER);
            rota.setSpacingBefore(18);
            rota.setSpacingAfter(2);
            documento.add(rota);

            Paragraph cidades = new Paragraph(
                    cidadeOrigem + " para " + cidadeDestino + " | " + itinerario,
                    fonte(10, false, COR_CINZA_LABEL));
            cidades.setAlignment(Element.ALIGN_CENTER);
            cidades.setSpacingAfter(16);
            documento.add(cidades);

            PdfPTable dados = new PdfPTable(4);
            dados.setWidthPercentage(100);
            dados.setSpacingAfter(16);
            adicionarCampo(dados, "DATA DO VOO", dataVoo);
            adicionarCampo(dados, "HORARIO", horarioVoo);
            adicionarCampo(dados, "PORTAO", portaoEmbarque);
            adicionarCampo(dados, "ASSENTO", assento);
            adicionarCampo(dados, "PASSAGEIRO", nomePassageiro);
            adicionarCampo(dados, "VOO", codigoVoo);
            adicionarCampo(dados, "VALOR", valorTotal == null ? "N/D" : "R$ " + valorTotal);
            adicionarCampo(dados, "STATUS", "CONFIRMADO");
            documento.add(dados);

            PdfPTable rodape = new PdfPTable(new float[]{3f, 2f});
            rodape.setWidthPercentage(100);
            PdfPCell codigo = criarCelula("CODIGO DO E-TICKET\n" + numTicket, 11, true, COR_GRAFITE, COR_CINZA_CLARO);
            codigo.setPadding(12);
            PdfPCell embarque = criarCelula(
                    siglaOrigem + " -> " + siglaDestino + "\n" + dataVoo + " " + horarioVoo,
                    12, true, COR_AZUL_MARINHO, COR_CINZA_CLARO);
            embarque.setHorizontalAlignment(Element.ALIGN_RIGHT);
            embarque.setPadding(12);
            rodape.addCell(codigo);
            rodape.addCell(embarque);
            documento.add(rodape);

            documento.close();
            System.out.println("E-ticket gerado com sucesso: " + arquivoPdf.getAbsolutePath());
            return arquivoPdf;

        } catch (DocumentException | IOException e) {
            System.err.println("Erro ao gerar e-ticket PDF: " + e.getMessage());
            if (documento.isOpen()) {
                documento.close();
            }
            return null;
        }
    }

    private static void adicionarCampo(PdfPTable tabela, String rotulo, String valor) {
        PdfPCell celula = criarCelula(rotulo + "\n" + valor, 10, true, COR_GRAFITE, COR_BRANCO);
        celula.setPadding(10);
        tabela.addCell(celula);
    }

    private static PdfPCell criarCelula(String texto, int tamanhoFonte, boolean negrito,
                                        Color corTexto, Color corFundo) {
        Font fonte = fonte(tamanhoFonte, negrito, corTexto);
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setBorderColor(COR_LINHA_TRACEJADA);
        celula.setBackgroundColor(corFundo);
        celula.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return celula;
    }

    private static PdfPCell celulaLogo() throws IOException, BadElementException {
        java.net.URL url = GeradorPdf.class.getResource(LOGO);
        if (url == null) {
            return criarCelula("DJE AIRLINES\nCARTAO DE EMBARQUE", 18, true, COR_BRANCO, COR_AZUL_MARINHO);
        }
        Image logo = Image.getInstance(url);
        logo.scaleToFit(150, 54);
        PdfPCell celula = new PdfPCell(logo, false);
        celula.setBorderColor(COR_LINHA_TRACEJADA);
        celula.setBackgroundColor(COR_AZUL_MARINHO);
        celula.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celula.setHorizontalAlignment(Element.ALIGN_LEFT);
        return celula;
    }

    private static Font fonte(int tamanho, boolean negrito, Color cor) {
        String caminho = negrito ? FONTE_NEGRITO : FONTE_NORMAL;
        if (Files.isRegularFile(Path.of(caminho))) {
            try {
                BaseFont base = BaseFont.createFont(caminho, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                return new Font(base, tamanho, negrito ? Font.BOLD : Font.NORMAL, cor);
            } catch (DocumentException | IOException e) {
                return FontFactory.getFont(negrito ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA,
                        tamanho, cor);
            }
        }
        return FontFactory.getFont(negrito ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA,
                tamanho, cor);
    }

    private static String extrairSigla(String aeroporto) {
        int abre = aeroporto.lastIndexOf('(');
        int fecha = aeroporto.lastIndexOf(')');
        if (abre >= 0 && fecha > abre) {
            return aeroporto.substring(abre + 1, fecha).trim().toUpperCase();
        }
        return aeroporto.length() >= 3
                ? aeroporto.substring(0, 3).toUpperCase()
                : aeroporto.toUpperCase();
    }

    private static String extrairCidade(String aeroporto) {
        int abre = aeroporto.lastIndexOf('(');
        if (abre > 0) {
            return aeroporto.substring(0, abre).trim();
        }
        return aeroporto;
    }

}
