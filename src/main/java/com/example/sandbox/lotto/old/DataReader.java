package com.example.sandbox.lotto.old;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataReader {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d M yyyy");

    public static List<Ziehung> readData(Path filePath) throws IOException {
        try (Stream<String> lines = Files.lines(filePath)) {
            return lines.skip(1) // Kopfzeile überspringen
                    .filter(line -> !line.startsWith("#"))
                    .map(line -> {
                        String[] parts = line.split("\\t"); // Zeile anhand des Tab-Separators aufteilen

                        // Datum und Uhrzeit erstellen
                        String dateString = parts[0] + " " + parts[1] + " " + parts[2];
                        LocalDate dateTime = LocalDate.parse(dateString, FORMATTER);

                        // Die Zahlen 1 bis 6 in eine Liste packen
                        List<Integer> zahlen = Stream.of(parts[3], parts[4], parts[5], parts[6], parts[7], parts[8])
                                .map(Integer::parseInt)
                                .sorted()
                                .collect(Collectors.toList());

                        // Zusatz- und Superzahl parsen.
                        // AB 2013-05-01 gibt es keinen Zusatz Zahl mehr.
                        Optional<Integer> zusatzOptional = safeParse(parts[9]);
                        Integer zusatz = zusatzOptional.orElse(-1);
                        Integer superzahl = Integer.parseInt(parts[10]);

                        return new Ziehung(dateTime, zahlen, zusatz, superzahl);
                    })
                    .collect(Collectors.toList());
        }
    }

    public static Optional<Integer> safeParse(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static void main(String[] args) {
        // Beispiel-Nutzung
        Path filePath = Path.of("path/to/your/file.txt"); // Den Pfad zur Datei anpassen
        try {
            List<Ziehung> ziehungen = readData(filePath);
            ziehungen.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}