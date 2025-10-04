package com.example.sandbox.lottery.lotto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record LottoZiehung(
        LocalDate datum,
        List<Integer> hauptZahlen,
        Optional<Integer> zusatzzahl,    // Z.B. alte Lotto-Zusatzzahl
        Optional<Integer> superzahl,     // Z.B. Superzahl bei 6aus49
        List<Integer> euroZahlen         // Z.B. 2 Eurozahlen bei Eurojackpot
) {
    // Du kannst hier einen Compact Constructor hinzufügen, um sicherzustellen,
    // dass die Listen und Optional-Werte niemals null sind.
}