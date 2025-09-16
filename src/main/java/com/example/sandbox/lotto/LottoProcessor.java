package com.example.sandbox.lotto;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class LottoProcessor {

    public static Lotto6aus49 processLine(String line) {
        // Die Zeile am Tabulator trennen, wie es bei diesem Format oft der Fall ist
        String[] parts = line.split("\t");

        // Das Datum aus Tag, Monat und Jahr zusammensetzen und parsen
        int tag = Integer.parseInt(parts[0].trim());
        int monat = Integer.parseInt(parts[1].trim());
        int jahr = Integer.parseInt(parts[2].trim());
        LocalDate datum = LocalDate.of(jahr, monat, tag);

        // Die sechs Hauptzahlen parsen
        List<Integer> zahlen = Arrays.stream(parts, 3, 9)
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();

        // Die Zusatzzahl und die Superzahl parsen
        int zusatzzahl = Integer.parseInt(parts[9].trim());
        int superzahl = Integer.parseInt(parts[10].trim());

        // Das neue Lotto6aus49-Objekt erstellen und zurückgeben
        return new Lotto6aus49(datum, zahlen, zusatzzahl, superzahl);
    }

    public static void main(String[] args) {
        String dataLine = "2\t12\t2000\t46\t4\t45\t32\t42\t43\t35\t5";
        Lotto6aus49 ergebnis = processLine(dataLine);

        System.out.println("Datum: " + ergebnis.datum());
        System.out.println("Zahlen: " + ergebnis.zahlen());
        System.out.println("Zusatzzahl: " + ergebnis.zusatzzahl());
        System.out.println("Superzahl: " + ergebnis.superzahl());
    }
}