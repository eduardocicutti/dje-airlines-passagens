package br.ufes.passagens.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class GeradorTicket {

    public static String gerarNumeroETicket(String nomePassageiro, String codigoVoo, String dataVoo) {
        return gerarNumeroETicket(nomePassageiro, "SEM-DOCUMENTO", codigoVoo, dataVoo, "SEM-VENDA");
    }

    public static String gerarNumeroETicket(String nomePassageiro, String documento,
                                             String codigoVoo, String dataVoo, Object idVenda) {
        String entradaDados = String.join("|", nomePassageiro, documento, codigoVoo,
                dataVoo, String.valueOf(idVenda)).replaceAll("\\s+", "").toLowerCase();
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytesHash = digest.digest(entradaDados.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexadecimal = new StringBuilder();
            for (byte b : bytesHash) {
                String parte = Integer.toHexString(0xff & b);
                if (parte.length() == 1) hexadecimal.append('0');
                hexadecimal.append(parte);
            }
            
            return hexadecimal.toString().substring(0, 12).toUpperCase();
            
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Erro ao gerar código, usando valor alternativo por horário: " + e.getMessage());
            return "TK-" + System.currentTimeMillis();
        }
    }
}
