package com.example.sandbox.lottery.euro;

import com.example.sandbox.lottery.lotto.LottoZiehung;
import com.example.sandbox.lottery.lotto.Processor;
import com.example.sandbox.functional.Either;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EurolottoProcessor implements Processor {
    private static final int MAX_LOTTO_NUMBER = 50;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d M yyyy");

    @Override
    public Either<String, LottoZiehung> processLine(String line) {
        try {
            String[] parts = line.split("\\t");
            if (parts.length < 10) {
                return Either.left("Zeile hat nicht die erwartete Anzahl an Spalten: " + line);
            }

            // Parsen des Datums
            String dateString = parts[0] + " " + parts[1] + " " + parts[2];
            LocalDate datum = LocalDate.parse(dateString, FORMATTER);

            // Parsen der sechs Hauptzahlen
            List<Integer> zahlen = Arrays.stream(parts, 3, 8)
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .sorted()
                    .toList();

            // Parsen der sechs Hauptzahlen
            List<Integer> eurozahlen = Arrays.stream(parts, 8, 10)
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .sorted()
                    .toList();

            return Either.right(new LottoZiehung(datum, zahlen, Optional.empty(), Optional.empty(), eurozahlen));

        } catch (Exception e) {
            // Fängt NumberFormatException oder andere Fehler ab
            return Either.left("Fehler beim Parsen der Zeile '" + line + "': " + e.getMessage());
        }
    }

    private static Optional<Integer> safeParse(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            // Optional.empty() zurückgeben, wenn der String keine Zahl ist
            return Optional.empty();
        }
    }

    @Override
    public int getMaxLottoNumber() {
        return MAX_LOTTO_NUMBER;
    }
}