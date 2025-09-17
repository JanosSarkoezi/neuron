package com.example.sandbox.lotto;

import com.example.sandbox.validator.Either;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class DataReader {

    public static Either<String, List<LottoZiehung>> readData(Path filePath) {
        System.out.println("Lese Daten aus der Datei: " + filePath.toAbsolutePath());
        try (Stream<String> lines = Files.lines(filePath)) {
            // Die einzelnen Zeilen parsen und in eine Liste von Eithers umwandeln
            List<Either<String, LottoZiehung>> allResults = lines
                    .skip(1)
                    .filter(line -> !line.startsWith("#"))
                    .map(LottoProcessor::processLine)
                    .toList();

            // Mithilfe von Either.sequence die Liste der Eithers in einen einzigen Either konvertieren.
            // Der erste Fehler bricht die Operation ab.
            return Either.sequence(allResults);

        } catch (IOException e) {
            return Either.left("Fehler beim Lesen der Datei: " + e.getMessage());
        }
    }
}