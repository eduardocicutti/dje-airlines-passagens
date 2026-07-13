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
import java.text.NumberFormat;
import java.util.Locale;
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
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
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

        Document documento = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            Files.createDirectories(arquivoPdf.toPath().getParent());
            PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(arquivoPdf));
            documento.open();

            String siglaOrigem  = extrairSigla(origem);
            String siglaDestino = extrairSigla(destino);
            String cidadeOrigem = CIDADES.getOrDefault(siglaOrigem, extrairCidade(origem));
            String cidadeDestino = CIDADES.getOrDefault(siglaDestino, extrairCidade(destino));
            desenharETicket(writer.getDirectContent(),
                    nomePassageiro, codigoVoo, siglaOrigem, siglaDestino,
                    cidadeOrigem, cidadeDestino, dataVoo, horarioVoo,
                    numTicket, portaoEmbarque, assento, itinerario, valorTotal);

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

    private static void desenharETicket(PdfContentByte cb,
                                        String nomePassageiro,
                                        String codigoVoo,
                                        String siglaOrigem,
                                        String siglaDestino,
                                        String cidadeOrigem,
                                        String cidadeDestino,
                                        String dataVoo,
                                        String horarioVoo,
                                        String numTicket,
                                        String portaoEmbarque,
                                        String assento,
                                        String itinerario,
                                        BigDecimal valorTotal) throws DocumentException, IOException {
        BaseFont normal = baseFont(false);
        BaseFont negrito = baseFont(true);

        retangulo(cb, 0, 0, 595, 842, COR_CINZA_CLARO, null);
        retangulo(cb, 42, 92, 511, 660, COR_BRANCO, COR_LINHA_TRACEJADA);

        retangulo(cb, 42, 690, 511, 62, COR_AZUL_MARINHO, null);
        texto(cb, "DJE AIRLINES", negrito, 22, COR_BRANCO, 66, 726, Element.ALIGN_LEFT);
        texto(cb, "CARTAO DE EMBARQUE", normal, 10, COR_BRANCO, 66, 709, Element.ALIGN_LEFT);
        texto(cb, "E-TICKET", negrito, 20, COR_BRANCO, 529, 726, Element.ALIGN_RIGHT);
        texto(cb, numTicket, negrito, 13, COR_BRANCO, 529, 708, Element.ALIGN_RIGHT);

        texto(cb, siglaOrigem + "  >  " + siglaDestino, negrito, 44, COR_AZUL_MARINHO, 297, 640, Element.ALIGN_CENTER);
        texto(cb, cidadeOrigem + " para " + cidadeDestino, normal, 13, COR_CINZA_LABEL, 297, 615, Element.ALIGN_CENTER);
        texto(cb, encurtar(itinerario, 62), normal, 10, COR_CINZA_LABEL, 297, 598, Element.ALIGN_CENTER);

        campo(cb, "PASSAGEIRO", encurtar(nomePassageiro, 32), 66, 535, 220, normal, negrito);
        campo(cb, "VOO", codigoVoo, 322, 535, 90, normal, negrito);
        campo(cb, "STATUS", "CONFIRMADO", 440, 535, 90, normal, negrito);

        retangulo(cb, 66, 350, 463, 132, COR_BRANCO, COR_LINHA_TRACEJADA);
        linha(cb, 66, 416, 529, 416, COR_LINHA_TRACEJADA);
        linha(cb, 181, 350, 181, 482, COR_LINHA_TRACEJADA);
        linha(cb, 296, 350, 296, 482, COR_LINHA_TRACEJADA);
        linha(cb, 411, 350, 411, 482, COR_LINHA_TRACEJADA);

        campo(cb, "DATA", dataVoo, 82, 445, 80, normal, negrito);
        campo(cb, "HORARIO", horarioVoo, 197, 445, 80, normal, negrito);
        campo(cb, "PORTAO", portaoEmbarque, 312, 445, 80, normal, negrito);
        campo(cb, "ASSENTO", assento, 427, 445, 80, normal, negrito);

        campo(cb, "ORIGEM", siglaOrigem, 82, 379, 96, normal, negrito);
        campo(cb, "DESTINO", siglaDestino, 197, 379, 96, normal, negrito);
        campo(cb, "VALOR", valorTotal == null ? "N/D" : MOEDA.format(valorTotal), 312, 379, 96, normal, negrito);
        campo(cb, "CLASSE", "ECONOMICA", 427, 379, 96, normal, negrito);

        retangulo(cb, 66, 214, 463, 88, COR_CINZA_CLARO, COR_LINHA_TRACEJADA);
        texto(cb, "CODIGO DO E-TICKET", normal, 10, COR_CINZA_LABEL, 86, 269, Element.ALIGN_LEFT);
        texto(cb, numTicket, negrito, 19, COR_GRAFITE, 86, 245, Element.ALIGN_LEFT);
        texto(cb, siglaOrigem + " -> " + siglaDestino, negrito, 18, COR_AZUL_MARINHO, 509, 264, Element.ALIGN_RIGHT);
        texto(cb, dataVoo + " " + horarioVoo, negrito, 14, COR_GRAFITE, 509, 242, Element.ALIGN_RIGHT);

        desenharCodigoBarras(cb, 86, 134, 230, 42);
        texto(cb, "Apresente este documento no embarque.", normal, 10, COR_CINZA_LABEL, 509, 153, Element.ALIGN_RIGHT);
        texto(cb, "Documento gerado automaticamente pelo sistema DJE Airlines.", normal, 9, COR_CINZA_LABEL, 297, 106, Element.ALIGN_CENTER);
    }

    private static void campo(PdfContentByte cb, String rotulo, String valor,
                              float x, float y, float largura,
                              BaseFont normal, BaseFont negrito) {
        String valorTratado = encurtar(valor, largura > 100 ? 32 : 20);
        int tamanhoValor = valorTratado.length() > 14 ? 12 : 15;
        texto(cb, rotulo, normal, 9, COR_CINZA_LABEL, x, y, Element.ALIGN_LEFT);
        texto(cb, valorTratado, negrito, tamanhoValor, COR_GRAFITE, x, y - 20, Element.ALIGN_LEFT);
    }

    private static void texto(PdfContentByte cb, String texto, BaseFont fonte, float tamanho,
                              Color cor, float x, float y, int alinhamento) {
        cb.saveState();
        cb.beginText();
        cb.setFontAndSize(fonte, tamanho);
        cb.setColorFill(cor);
        cb.showTextAligned(alinhamento, texto == null ? "" : texto, x, y, 0);
        cb.endText();
        cb.restoreState();
    }

    private static void retangulo(PdfContentByte cb, float x, float y, float w, float h,
                                  Color preenchimento, Color borda) {
        cb.saveState();
        if (preenchimento != null) {
            cb.setColorFill(preenchimento);
            cb.rectangle(x, y, w, h);
            cb.fill();
        }
        if (borda != null) {
            cb.setColorStroke(borda);
            cb.rectangle(x, y, w, h);
            cb.stroke();
        }
        cb.restoreState();
    }

    private static void linha(PdfContentByte cb, float x1, float y1, float x2, float y2, Color cor) {
        cb.saveState();
        cb.setColorStroke(cor);
        cb.moveTo(x1, y1);
        cb.lineTo(x2, y2);
        cb.stroke();
        cb.restoreState();
    }

    private static void desenharCodigoBarras(PdfContentByte cb, float x, float y, float largura, float altura) {
        cb.saveState();
        cb.setColorFill(COR_GRAFITE);
        float atual = x;
        int[] barras = {2, 1, 3, 1, 1, 2, 4, 1, 2, 3, 1, 1, 3, 2, 1, 4, 2, 1, 2, 3, 1, 2, 4, 1};
        for (int i = 0; i < barras.length && atual < x + largura; i++) {
            float w = barras[i] * 1.7f;
            if (i % 2 == 0) {
                cb.rectangle(atual, y, w, altura);
                cb.fill();
            }
            atual += w + 1.2f;
        }
        cb.restoreState();
    }

    private static String encurtar(String texto, int maximo) {
        if (texto == null) return "";
        return texto.length() <= maximo ? texto : texto.substring(0, Math.max(0, maximo - 3)) + "...";
    }

    private static BaseFont baseFont(boolean negrito) throws DocumentException, IOException {
        String caminho = negrito ? FONTE_NEGRITO : FONTE_NORMAL;
        if (Files.isRegularFile(Path.of(caminho))) {
            return BaseFont.createFont(caminho, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        }
        return BaseFont.createFont(negrito ? BaseFont.HELVETICA_BOLD : BaseFont.HELVETICA,
                BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
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
