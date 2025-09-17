package com.example.sandbox.euro;

import com.example.sandbox.lotto.LottoZiehung;
import com.example.sandbox.lotto.Processor;
import com.example.sandbox.validator.Either;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

public class EurolottoProcessor implements Processor {
    private static final int MAX_LOTTO_NUMBER = 50;

    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\s*;\\s*");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public Either<String, LottoZiehung> processLine(String line) {
        try {
            String[] parts = SPLIT_PATTERN.split(line);

            // Parsen des Datums
            LocalDate datum = LocalDate.parse(parts[0].trim(), DATE_FORMATTER);

            // Parsen der 5 Hauptzahlen (Indizes 1 bis 5)
            List<Integer> hauptzahlen = Arrays.stream(parts, 1, 6)
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();

            // Parsen der 2 Eurozahlen (Indizes 6 bis 7)
            List<Integer> eurozahlen = Arrays.stream(parts, 6, 8)
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();

            return Either.right(new LottoZiehung(datum, hauptzahlen, Optional.empty(), Optional.empty(), Collections.emptyList()));

        } catch (Exception e) {
            // Fängt NumberFormatException oder andere Fehler ab
            return Either.left("Fehler beim Parsen der Zeile '" + line + "': " + e.getMessage());
        }
    }

    @Override
    public int getMaxLottoNumber() {
        return MAX_LOTTO_NUMBER;
    }
}