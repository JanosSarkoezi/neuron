package com.example.sandbox.markov;

import com.example.sandbox.lotto.Lotto6aus49;
import com.example.sandbox.lotto.LottoProcessor;
import com.example.sandbox.neuron.Matrix;
import com.example.sandbox.validator.Either;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class LottoMarkov {

    public static void main(String[] args) {
        // Starte die Kette mit der Operation, die den Dateipfad liefert
        Either<String, Path> pathEither = getFilePath("lotto_6aus49_ab_02.12.2000.txt");

        // Die gesamte Verarbeitungskette als eine Folge von flatMap-Aufrufen
        Either<String, Matrix> resultEither = pathEither
                .flatMap(LottoMarkov::readAllData)
                .flatMap(LottoMarkov::filterByDate)
                .flatMap(LottoMarkov::buildMarkovMatrix);

        // Am Ende der Kette wird das Ergebnis verarbeitet
        resultEither.match(LottoMarkov::handleError, LottoMarkov::handleSuccess);
    }

    private static Either<String, Path> getFilePath(String fileName) {
        try {
            ClassLoader classLoader = LottoMarkov.class.getClassLoader();
            URI uri = Objects.requireNonNull(classLoader.getResource(fileName)).toURI();
            return Either.right(Path.of(uri));
        } catch (Exception e) {
            return Either.left("Fehler beim Abrufen des Dateipfads: " + e.getMessage());
        }
    }

    private static Either<String, List<Lotto6aus49>> readAllData(Path filePath) {
        System.out.println("Lese Daten aus der Datei: " + filePath.toAbsolutePath());
        try (Stream<String> lines = Files.lines(filePath)) {
            // Die einzelnen Zeilen parsen und in eine Liste von Eithers umwandeln
            List<Either<String, Lotto6aus49>> allResults = lines
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

    private static Either<String, List<Lotto6aus49>> filterByDate(List<Lotto6aus49> ziehungen) {
        LocalDate filterDatum = LocalDate.of(2020, 1, 1);
        System.out.println("Anzahl aller gelesenen Ziehungen: " + ziehungen.size());
        List<Lotto6aus49> gefilterteZiehungen = ziehungen.stream()
                .filter(ziehung -> ziehung.datum().isAfter(filterDatum) || ziehung.datum().isEqual(filterDatum))
                .toList();
        System.out.println("Anzahl der Ziehungen nach dem " + filterDatum + ": " + gefilterteZiehungen.size());
        return Either.right(gefilterteZiehungen);
    }

    private static Either<String, Matrix> buildMarkovMatrix(List<Lotto6aus49> gefilterteZiehungen) {
        System.out.println("Erstelle die Übergangsmatrix mit den gefilterten Daten...");
        int maxLottoNumber = 49;
        MarkovMatrix markov = new MarkovMatrix(maxLottoNumber);

        gefilterteZiehungen.stream()
                .map(Lotto6aus49::zahlen)
                .forEach(markov::addZiehung);

        Matrix probabilityMatrix = markov.buildProbabilityMatrix();
        return Either.right(probabilityMatrix);
    }

    private static void handleError(String error) {
        System.err.println("Ein Fehler ist aufgetreten: " + error);
    }

    private static void handleSuccess(Matrix probabilityMatrix) {
        System.out.println("Markov-Matrix erfolgreich erstellt.");
        System.out.println("Die Matrix hat die Dimension " + probabilityMatrix.rows() + "x" + probabilityMatrix.cols() + ".");
        // Hier könntest du die Matrix auch ausgeben oder weiterverarbeiten.
    }
}