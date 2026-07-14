package br.ufes.passagens.config;

import br.ufes.passagens.model.Voo;
import br.ufes.passagens.repository.GerenciadorDados;

import java.util.List;

public final class TesteConsultaVoos {
    private TesteConsultaVoos() {
    }

    public static void main(String[] args) throws Exception {
        GerenciadorDados dados = new GerenciadorDados();
        String[] aeroportos = {"BSB", "CGH", "CNF", "FLN", "FOR", "VIX"};
        int combinacoes = 0;

        for (String origem : aeroportos) {
            for (String destino : aeroportos) {
                if (!origem.equals(destino)) {
                    listar(dados, origem, destino);
                    combinacoes++;
                }
            }
        }
        System.out.println("Combinações verificadas: " + combinacoes);
    }

    private static void listar(GerenciadorDados dados, String origem, String destino) throws Exception {
        List<Voo> voos = dados.obterVoosDisponiveis(origem, destino);
        if (voos.size() != 2) {
            throw new IllegalStateException(origem + " -> " + destino
                    + " deveria ter 2 voos, mas possui " + voos.size() + ".");
        }
        System.out.println(origem + " -> " + destino + ": " + voos.size() + " voo(s)");
        for (Voo voo : voos) {
            System.out.println("  " + voo.getCodigo() + " " + voo.getData()
                    + " " + voo.getHorario() + " vagas=" + voo.getPoltronasVagas());
        }
    }
}
