package br.ufes.passagens.service;

import java.time.LocalDate;

@FunctionalInterface
public interface CalendarioFeriados {
    boolean ehFeriadoNacional(LocalDate data);
}
