package br.ufes.passagens.service;

import br.ufes.passagens.model.Aeroporto;
import br.ufes.passagens.model.Passageiro;
import br.ufes.passagens.model.Passagem;
import br.ufes.passagens.model.Trecho;
import br.ufes.passagens.model.Voo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculadoraPrecoTest {
    private static final LocalDate HOJE = LocalDate.of(2026, 7, 13);
    private CalculadoraPreco calculadora;

    @BeforeEach
    void preparar() {
        Clock relogio = Clock.fixed(Instant.parse("2026-07-13T12:00:00Z"), ZoneId.of("UTC"));
        calculadora = new CalculadoraPreco(relogio, data -> false);
    }

    @ParameterizedTest
    @CsvSource({"1,0.36", "500,0.36", "501,0.29", "800,0.29", "801,0.25"})
    void aplicaFaixasDeMilha(int distancia, String esperado) {
        assertEquals(new BigDecimal(esperado), calculadora.fatorMilha(distancia));
    }

    @ParameterizedTest
    @CsvSource({
            "0,4.52", "3,4.52", "4,3.21", "6,3.21", "7,2.25", "10,2.25",
            "11,1.98", "15,1.98", "16,1.78", "20,1.78", "21,1.65", "30,1.65", "31,1.45"
    })
    void aplicaFaixasDeAntecedencia(long dias, String esperado) {
        assertEquals(new BigDecimal(esperado), calculadora.fatorPeriodo(HOJE.plusDays(dias)));
    }

    @Test
    void rejeitaVooNoPassado() {
        assertThrows(IllegalArgumentException.class, () -> calculadora.fatorPeriodo(HOJE.minusDays(1)));
    }

    @ParameterizedTest
    @CsvSource({"0,1.09", "2,1.09", "3,1.05", "5,1.05", "6,1.02", "8,1.02", "9,1.00"})
    void aplicaFaixasDeRetorno(long dias, String esperado) {
        assertEquals(new BigDecimal(esperado), calculadora.fatorRetorno(HOJE, HOJE.plusDays(dias)));
    }

    @ParameterizedTest
    @CsvSource({
            "91,100,0.75", "90,100,0.85", "70,100,0.85", "69,100,0.95",
            "60,100,0.95", "59,100,1.00", "40,100,1.00", "39,100,1.15",
            "20,100,1.15", "19,100,1.20", "10,100,1.20", "9,100,1.35", "0,100,1.35"
    })
    void aplicaFaixasDeProcura(int vagas, int capacidade, String esperado) {
        assertEquals(new BigDecimal(esperado), calculadora.fatorProcura(vagas, capacidade));
    }

    @Test
    void somaTrechosDeUmaConexao() {
        Aeroporto fln = new Aeroporto("FLN", "Florianópolis", "Aeroporto de Florianópolis");
        Aeroporto cgh = new Aeroporto("CGH", "São Paulo", "Congonhas");
        Aeroporto bsb = new Aeroporto("BSB", "Brasília", "Aeroporto de Brasília");
        Voo voo = new Voo(1L, "DJE101", HOJE.plusDays(7), LocalTime.of(8, 30), "A1",
                100, 50, List.of(new Trecho(fln, cgh, 304), new Trecho(cgh, bsb, 541)));
        Passagem passagem = new Passagem(voo, new Passageiro(null, "Ana Lima", "12345678900"), "A1", null);

        assertEquals(new BigDecimal("599.24"), calculadora.calcularValorTotal(passagem));
    }
}
