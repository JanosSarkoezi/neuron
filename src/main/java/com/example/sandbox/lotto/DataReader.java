package com.example.sandbox.lotto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataReader {

    public static List<Lotto6aus49> readData(Path filePath) throws IOException {
        try (Stream<String> lines = Files.lines(filePath)) {
            return lines.skip(1) // Kopfzeile überspringen
                    .filter(line -> !line.startsWith("#")) // Kommentierte Zeilen ignorieren
                    .map(LottoProcessor::processLine) // Die Verarbeitung an den Processor delegieren
                    .collect(Collectors.toList());
        }
    }

    public static void main(String[] args) {
        // Beispiel-Nutzung
        Path filePath = Path.of("path/to/your/file.txt"); // Den Pfad zur Datei anpassen
        try {
            List<Lotto6aus49> ziehungen = readData(filePath);
            ziehungen.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}