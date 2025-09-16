package com.example.sandbox.euro;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

public record EuroLottoZiehung(
        LocalDate datum,
        List<Integer> hauptzahlen, // Die 5 Hauptzahlen
        List<Integer> eurozahlen, // Die 2 Eurozahlen
        BigDecimal spieleinsatz,
        BigDecimal jackpot,
        List<Gewinnklasse> gewinnklassen
) {
    public record Gewinnklasse(
            int anzahlGewinner,
            BigDecimal quote
    ) {}
}