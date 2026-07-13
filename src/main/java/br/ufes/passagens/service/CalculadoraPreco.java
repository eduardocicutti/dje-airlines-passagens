package br.ufes.passagens.service;

import br.ufes.passagens.model.Passagem;
import br.ufes.passagens.model.Trecho;
import br.ufes.passagens.model.Voo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class CalculadoraPreco {
    private static final int ESCALA_INTERNA = 8;
    private final Clock relogio;
    private final CalendarioFeriados calendarioFeriados;

    public CalculadoraPreco(Clock relogio, CalendarioFeriados calendarioFeriados) {
        this.relogio = Objects.requireNonNull(relogio);
        this.calendarioFeriados = Objects.requireNonNull(calendarioFeriados);
    }

    public BigDecimal calcularValorTotal(Passagem passagem) {
        Objects.requireNonNull(passagem, "Passagem obrigatória.");
        Voo voo = passagem.getVoo();
        BigDecimal total = BigDecimal.ZERO;
        for (Trecho trecho : voo.getTrechos()) {
            total = total.add(calcularTrecho(trecho.getDistanciaMilhas(), voo,
                    passagem.getDataRetorno()));
        }
        total = total.setScale(2, RoundingMode.HALF_UP);
        passagem.setValorTotal(total);
        return total;
    }

    BigDecimal calcularTrecho(int distancia, Voo voo, LocalDate dataRetorno) {
        if (distancia <= 0) {
            throw new IllegalArgumentException("Não existe distância válida para o trecho.");
        }
        return BigDecimal.valueOf(distancia)
                .multiply(fatorMilha(distancia))
                .multiply(fatorPeriodo(voo.getData()))
                .multiply(fatorDuffs(voo.getData()))
                .multiply(fatorRetorno(voo.getData(), dataRetorno))
                .multiply(fatorProcura(voo.getPoltronasVagas(), voo.getCapacidade()))
                .setScale(ESCALA_INTERNA, RoundingMode.HALF_UP);
    }

    BigDecimal fatorMilha(int distancia) {
        if (distancia <= 0) throw new IllegalArgumentException("Distância inválida.");
        if (distancia <= 500) return bd("0.36");
        if (distancia <= 800) return bd("0.29");
        return bd("0.25");
    }

    BigDecimal fatorPeriodo(LocalDate dataVoo) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(relogio), dataVoo);
        if (dias < 0) throw new IllegalArgumentException("A data do voo não pode estar no passado.");
        if (dias <= 3) return bd("4.52");
        if (dias <= 6) return bd("3.21");
        if (dias <= 10) return bd("2.25");
        if (dias <= 15) return bd("1.98");
        if (dias <= 20) return bd("1.78");
        if (dias <= 30) return bd("1.65");
        return bd("1.45");
    }

    BigDecimal fatorDuffs(LocalDate dataVoo) {
        if (calendarioFeriados.ehFeriadoNacional(dataVoo)) return bd("3.56");
        DayOfWeek dia = dataVoo.getDayOfWeek();
        return dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY ? bd("1.21") : bd("1.00");
    }

    BigDecimal fatorRetorno(LocalDate dataVoo, LocalDate dataRetorno) {
        if (dataRetorno == null) return bd("1.00");
        long dias = ChronoUnit.DAYS.between(dataVoo, dataRetorno);
        if (dias < 0) throw new IllegalArgumentException("Retorno não pode ocorrer antes da ida.");
        if (dias <= 2) return bd("1.09");
        if (dias <= 5) return bd("1.05");
        if (dias <= 8) return bd("1.02");
        return bd("1.00");
    }

    BigDecimal fatorProcura(int vagas, int capacidade) {
        if (capacidade <= 0 || vagas < 0 || vagas > capacidade) {
            throw new IllegalArgumentException("Ocupação do voo inválida.");
        }
        BigDecimal percentual = BigDecimal.valueOf(vagas)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(capacidade), ESCALA_INTERNA, RoundingMode.HALF_UP);
        if (percentual.compareTo(bd("90")) > 0) return bd("0.75");
        if (percentual.compareTo(bd("70")) >= 0) return bd("0.85");
        if (percentual.compareTo(bd("60")) >= 0) return bd("0.95");
        if (percentual.compareTo(bd("40")) >= 0) return bd("1.00");
        if (percentual.compareTo(bd("20")) >= 0) return bd("1.15");
        if (percentual.compareTo(bd("10")) >= 0) return bd("1.20");
        return bd("1.35");
    }

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }
}
