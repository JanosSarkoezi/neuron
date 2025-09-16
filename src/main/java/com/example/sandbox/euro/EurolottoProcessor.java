package com.example.sandbox.euro;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class EurolottoProcessor {

    // Record für die Datenstruktur
    public record EuroLottoZiehung(
            LocalDate datum,
            List<Integer> hauptzahlen,
            List<Integer> eurozahlen,
            BigDecimal spieleinsatz,
            BigDecimal jackpot,
            List<Gewinnklasse> gewinnklassen
    ) {
        public record Gewinnklasse(
                long anzahlGewinner,
                BigDecimal quote
        ) {}
    }

    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\s*;\\s*");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static EuroLottoZiehung parseCsvLine(String line) throws ParseException {
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

        // Parsen der finanziellen Werte
        BigDecimal spieleinsatz = new BigDecimal(parts[8].trim().replace(".", "").replace(",", "."));

        // Da die Jackpot-Information in der Beispieldatei nicht ganz klar war,
        // nehmen wir den Wert aus der Gewinnklasse 1 Quote, was in vielen
        // Fällen der Jackpot sein kann. Alternativ könnte es auch der zweite
        // Wert sein, wie im letzten Beispiel. Hier eine pragmatische Annahme.
        BigDecimal jackpot = new BigDecimal(parts[10].trim().replace(".", "").replace(",", "."));

        // Parsen der 12 Gewinnklassen
        List<EuroLottoZiehung.Gewinnklasse> gewinnklassen = new ArrayList<>();
        // Die Gewinnklassen beginnen bei Index 11.
        for (int i = 11; i < parts.length; i += 2) {
            long anzahlGewinner = Long.parseLong(parts[i].trim());
            BigDecimal quote = new BigDecimal(parts[i + 1].trim().replace(".", "").replace(",", "."));
            gewinnklassen.add(new EuroLottoZiehung.Gewinnklasse(anzahlGewinner, quote));
        }

        return new EuroLottoZiehung(datum, hauptzahlen, eurozahlen, spieleinsatz, jackpot, gewinnklassen);
    }

    // Die main-Methode mit einem Beispiel
    public static void main(String[] args) {
        String csvLine = "07.01.2022;47;23;21;17; 8; 6; 2; 45.918.930,00;   Jackpot; 23.310.842,80;       5;    390.310,90;       7;     98.397,70;       76;      3.020,90;        886;         233,20;       1.713;         93,80;       2.661;          51,70;       37.840;          18,80;       41.383;          16,60;       75.709;          13,00;      190.762;           9,30;      576.488;           7,60";

        try {
            EuroLottoZiehung ziehung = parseCsvLine(csvLine);
            System.out.println("--- EuroLotto-Ziehung verarbeitet ---");
            System.out.println("Datum: " + ziehung.datum());
            System.out.println("Hauptzahlen: " + ziehung.hauptzahlen());
            System.out.println("Eurozahlen: " + ziehung.eurozahlen());
            System.out.println("Spieleinsatz: " + ziehung.spieleinsatz() + " Euro");
            System.out.println("Jackpot: " + ziehung.jackpot() + " Euro");

            System.out.println("\n--- Gewinnklassen-Details ---");
            for (int i = 0; i < ziehung.gewinnklassen().size(); i++) {
                EuroLottoZiehung.Gewinnklasse gk = ziehung.gewinnklassen().get(i);
                System.out.println("Gewinnklasse " + (i + 1) + ":");
                System.out.println("  Anzahl Gewinner: " + gk.anzahlGewinner());
                System.out.println("  Gewinnquote: " + gk.quote() + " Euro");
            }

        } catch (ParseException e) {
            System.err.println("Fehler beim Parsen der Zeile: " + e.getMessage());
        }
    }
}