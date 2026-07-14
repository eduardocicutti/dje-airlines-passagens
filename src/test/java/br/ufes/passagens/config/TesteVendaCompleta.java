package br.ufes.passagens.config;

import br.ufes.passagens.model.AssentoResumo;
import br.ufes.passagens.model.VendaConfirmada;
import br.ufes.passagens.model.PagamentoDinheiro;
import br.ufes.passagens.model.Passageiro;
import br.ufes.passagens.model.Passagem;
import br.ufes.passagens.model.Voo;
import br.ufes.passagens.repository.GerenciadorDados;
import br.ufes.passagens.service.CalculadoraPreco;
import br.ufes.passagens.service.CalendarioFeriadosFixos;

import java.time.Clock;
import java.util.List;

public final class TesteVendaCompleta {
    private TesteVendaCompleta() {
    }

    public static void main(String[] args) throws Exception {
        GerenciadorDados dados = new GerenciadorDados();
        List<Voo> voos = dados.obterVoosDisponiveis("FLN", "BSB");
        if (voos.isEmpty()) {
            throw new IllegalStateException("Nenhum voo disponível para FLN -> BSB.");
        }

        Voo voo = voos.get(0);
        AssentoResumo assento = dados.obterAssentos(voo.getId()).stream()
                .filter(AssentoResumo::disponivel)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhum assento disponível."));

        Passageiro passageiro = new Passageiro(null,
                "Passageiro Teste",
                "TESTE" + System.currentTimeMillis());
        Passagem passagem = new Passagem(voo, passageiro, assento.codigo(), null);
        new CalculadoraPreco(Clock.systemDefaultZone(), new CalendarioFeriadosFixos())
                .calcularValorTotal(passagem);

        VendaConfirmada venda = dados.confirmarVenda(passagem,
                new PagamentoDinheiro(passagem.getValorTotal(), "1001"));

        System.out.println("Venda " + venda.vendaId()
                + " confirmada com e-ticket " + venda.numeroETicket()
                + " em " + venda.arquivoPdf().getAbsolutePath());
    }
}
