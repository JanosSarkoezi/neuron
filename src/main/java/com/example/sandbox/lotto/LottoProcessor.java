package com.example.sandbox.lotto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LottoProcessor {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d M yyyy");

    public static Lotto6aus49 processLine(String line) {
        String[] parts = line.split("\\t");

        // Datum zusammensetzen und parsen
        String dateString = parts[0] + " " + parts[1] + " " + parts[2];
        LocalDate datum = LocalDate.parse(dateString, FORMATTER);

        // Die sechs Lottozahlen parsen
        // Die sechs Hauptzahlen parsen
        List<Integer> zahlen = Arrays.stream(parts, 3, 9)
                .map(String::trim)
                .map(Integer::parseInt)
                .sorted()
                .toList();

        // Zusatz- und Superzahl parsen
        Optional<Integer> zusatz = safeParse(parts[9]);
        int superzahl = Integer.parseInt(parts[10]);

        return new Lotto6aus49(datum, zahlen, zusatz, superzahl);
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
        com.example.sandbox.lotto.Lotto6aus49 ergebnis = processLine(dataLine);

        System.out.println("Datum: " + ergebnis.datum());
        System.out.println("Zahlen: " + ergebnis.zahlen());
        System.out.println("Zusatzzahl: " + ergebnis.zusatzzahl());
        System.out.println("Superzahl: " + ergebnis.superzahl());
    }
}