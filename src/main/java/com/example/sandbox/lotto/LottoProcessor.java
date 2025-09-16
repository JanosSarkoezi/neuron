package com.example.sandbox.lotto;

import com.example.sandbox.validator.Either;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LottoProcessor {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d M yyyy");

    public static Either<String, Lotto6aus49> processLine(String line) {
        try {
            String[] parts = line.split("\\t");
            if (parts.length < 11) {
                return Either.left("Zeile hat nicht die erwartete Anzahl an Spalten: " + line);
            }

            // Parsen des Datums
            String dateString = parts[0] + " " + parts[1] + " " + parts[2];
            LocalDate datum = LocalDate.parse(dateString, FORMATTER);

            // Parsen der sechs Hauptzahlen
            List<Integer> zahlen = Arrays.stream(parts, 3, 9)
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .sorted()
                    .toList();

            // Parsen der Zusatz- und Superzahl
            Optional<Integer> zusatz = safeParse(parts[9]);
            int superzahl = Integer.parseInt(parts[10].trim());

            return Either.right(new Lotto6aus49(datum, zahlen, zusatz, superzahl));

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

    public static void main(String[] args) {
        String dataLine = "2\t12\t2000\t46\t4\t45\t32\t42\t43\t35\t5";
        Either<String, Lotto6aus49> ergebnis = processLine(dataLine);

        ergebnis.match(
                error -> System.err.println("Fehler beim Parsen der Zeile: " + error),
                ziehung -> {
                    System.out.println("Datum: " + ziehung.datum());
                    System.out.println("Zahlen: " + ziehung.zahlen());
                    System.out.println("Zusatzzahl: " + ziehung.zusatzzahl());
                    System.out.println("Superzahl: " + ziehung.superzahl());
                }
        );
    }
}