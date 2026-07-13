package br.ufes.passagens.service;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Set;

public final class CalendarioFeriadosFixos implements CalendarioFeriados {
    private static final Set<MonthDay> FERIADOS_FIXOS = Set.of(
            MonthDay.of(1, 1), MonthDay.of(4, 21), MonthDay.of(5, 1),
            MonthDay.of(9, 7), MonthDay.of(10, 12), MonthDay.of(11, 2),
            MonthDay.of(11, 15), MonthDay.of(11, 20), MonthDay.of(12, 25));

    @Override
    public boolean ehFeriadoNacional(LocalDate data) {
        return FERIADOS_FIXOS.contains(MonthDay.from(data));
    }
}
