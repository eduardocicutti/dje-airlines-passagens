package br.ufes.passagens.config;

import br.ufes.passagens.model.Voo;
import br.ufes.passagens.repository.GerenciadorDados;

import java.util.List;

public final class TesteConsultaVoos {
    private TesteConsultaVoos() {
    }

    public static void main(String[] args) throws Exception {
        GerenciadorDados dados = new GerenciadorDados();
        listar(dados, "FLN", "BSB");
        listar(dados, "CGH", "VIX");
        listar(dados, "CGH", "FOR");
    }

    private static void listar(GerenciadorDados dados, String origem, String destino) throws Exception {
        List<Voo> voos = dados.obterVoosDisponiveis(origem, destino);
        System.out.println(origem + " -> " + destino + ": " + voos.size() + " voo(s)");
        for (Voo voo : voos) {
            System.out.println("  " + voo.getCodigo() + " " + voo.getData()
                    + " " + voo.getHorario() + " vagas=" + voo.getPoltronasVagas());
        }
    }
}
