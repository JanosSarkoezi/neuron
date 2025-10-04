package com.example.sandbox.ki.markov;

import com.example.sandbox.lottery.lotto.LottoZiehung;
import com.example.sandbox.lottery.lotto.LottoProcessor;
import com.example.sandbox.lottery.lotto.Processor;
import com.example.sandbox.ki.neuron.Matrix;
import com.example.sandbox.functional.Either;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class LottoMarkov {

    // Die Records müssen hier verfügbar sein
    record SplitData(List<LottoZiehung> trainingData, LottoZiehung lastEntry) {}
    record FinalResult(Matrix prediction, List<LottoZiehung> trainingData) {}
    private record PredictionItem(int lottoNumber, double probability) {}

    public static void main(String[] args) {
        Processor processor = new LottoProcessor();
        // Processor processor = new EurolottoProcessor();


        // Starte die Kette mit der Operation, die den Dateipfad liefert
        Either<String, Path> pathEither = getFilePath("lotto_6aus49_ab_02.12.2000.txt");
        // Either<String, Path> pathEither = getFilePath("eurojackpot.txt");

        // Die gesamte Verarbeitungskette als eine Folge von flatMap-Aufrufen
        Either<String, FinalResult> resultEither = pathEither
                .flatMap(path -> LottoMarkov.readAllData(path, processor))
                .flatMap(LottoMarkov::filterByDate)
                .flatMap(LottoMarkov::splitData)
                .flatMap(data -> LottoMarkov.createPrediction(data, processor.getMaxLottoNumber()));

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

    private static Either<String, List<LottoZiehung>> readAllData(Path filePath, Processor processor) {
        System.out.println("Lese Daten aus der Datei: " + filePath.toAbsolutePath());
        try (Stream<String> lines = Files.lines(filePath)) {
            // Die einzelnen Zeilen parsen und in eine Liste von Eithers umwandeln
            List<Either<String, LottoZiehung>> allResults = lines
                    .skip(1)
                    .filter(line -> !line.startsWith("#"))
                    .map(processor::processLine)
                    .toList();

            // Mithilfe von Either.sequence die Liste der Eithers in einen einzigen Either konvertieren.
            // Der erste Fehler bricht die Operation ab.
            return Either.sequence(allResults);

        } catch (IOException e) {
            return Either.left("Fehler beim Lesen der Datei: " + e.getMessage());
        }
    }

    private static Either<String, List<LottoZiehung>> filterByDate(List<LottoZiehung> ziehungen) {
        LocalDate filterDatum = LocalDate.of(2025, 1, 1);
        System.out.println("Anzahl aller gelesenen Ziehungen: " + ziehungen.size());
        List<LottoZiehung> gefilterteZiehungen = ziehungen.stream()
                .filter(ziehung -> ziehung.datum().isAfter(filterDatum))
                .toList();
        System.out.println("Anzahl der Ziehungen nach dem " + filterDatum + ": " + gefilterteZiehungen.size());
        return Either.right(gefilterteZiehungen);
    }

    // Hinzufügen zu LottoMarkov.java

    private static Either<String, SplitData> splitData(List<LottoZiehung> gefilterteZiehungen) {
        if (gefilterteZiehungen.size() < 2) {
            return Either.left("Nicht genügend Daten nach dem Filtern für Training und Prognose.");
        }

        // Erstelle eine veränderbare Kopie
        List<LottoZiehung> mutableList = new ArrayList<>(gefilterteZiehungen);

        // Entferne den letzten Eintrag und speichere ihn
        int lastIndex = mutableList.size() - 1;
        LottoZiehung lastEntry = mutableList.remove(lastIndex);

        System.out.println("Daten aufgeteilt. Trainingsdaten: " + mutableList.size() +
                ", Letzter Eintrag: " + lastEntry.datum() + ": " + lastEntry.hauptZahlen());

        return Either.right(new SplitData(mutableList, lastEntry));
    }

    /**
     * Erstellt einen 49x1 Vektor, wobei die gezogenen Zahlen 1.0 sind, sonst 0.0.
     */
    private static Matrix toHotmapVector(LottoZiehung lottoZug, int maxLottoNumber) {
        Matrix hotmap = Matrix.zeros(maxLottoNumber, 1);

        for (int number : lottoZug.hauptZahlen()) {
            // Lottozahlen sind 1-basiert, Matrix-Indizes sind 0-basiert
            int index = number - 1;
            if (index >= 0 && index < maxLottoNumber) {
                hotmap.set(index, 0, 1.0);
            }
        }
        return hotmap;
    }

    private static Either<String, FinalResult> createPrediction(SplitData splitData, int maxLottoNumber) {
        // 1. Markov-Matrix aus Trainingsdaten erstellen (Teil 1)
        System.out.println("Erstelle die Übergangsmatrix mit den Trainingsdaten...");
        MarkovMatrix markov = new MarkovMatrix(maxLottoNumber);

        splitData.trainingData().stream()
                .map(LottoZiehung::hauptZahlen)
                .forEach(markov::addZiehung);

        Matrix probabilityMatrix = markov.buildProbabilityMatrix();

        // 2. Hotmap des letzten Eintrags erstellen (Teil 2)
        Matrix hotmapVector = toHotmapVector(splitData.lastEntry(), maxLottoNumber);

        // 3. Die Prognose berechnen (Matrix-Multiplikation)
        // Prognose = Hotmap_Vector^T . Matrix
        //            (1x49)      . (49x49)  = (1x49)

        Matrix predictionVector = hotmapVector.transpose().dot(probabilityMatrix);

        System.out.println("Prognose erfolgreich berechnet.");

        // 4. FinalResult zurückgeben
        return Either.right(new FinalResult(predictionVector, splitData.trainingData()));
    }

    private static void handleError(String error) {
        System.err.println("Ein Fehler ist aufgetreten: " + error);
    }

    private static void handleSuccess(FinalResult result) {
        System.out.println("Prozess erfolgreich abgeschlossen.");

        Matrix predictionVector = result.prediction(); // Ein 1x49 Matrix-Vektor

        List<PredictionItem> predictions = new ArrayList<>();

        // Iteriere über die Spalten (Index 0 bis 48, entsprechend Lottozahl 1 bis 49)
        for (int j = 0; j < predictionVector.cols(); j++) {
            double probability = predictionVector.get(0, j);

            // Füge nur die Stellen hinzu, die eine Wahrscheinlichkeit > 0.0 aufweisen
            if (probability > 0.0) {
                // Die Lottozahl ist der Index j + 1
                predictions.add(new PredictionItem(j + 1, probability));
            }
        }

        // Sortiere die Ergebnisse absteigend nach Wahrscheinlichkeit
        predictions.sort(Comparator.comparingDouble(PredictionItem::probability).reversed());

        System.out.println("\n--- Prognose für die nächste gezogene Zahl ---");
        System.out.println("Basierend auf " + result.trainingData().size() + " Ziehungen.");
        System.out.println("----------------------------------------------");

        int rank = 1;
        for (PredictionItem item : predictions) {
            // Ausgabe formatiert auf 6 Dezimalstellen
            System.out.printf("%2d. Zahl: %2d (Wahrscheinlichkeit: %.6f)\n",
                    rank++, item.lottoNumber(), item.probability());

            // Begrenze die Ausgabe auf die Top 10 für Übersichtlichkeit
            if (rank > 10) {
                System.out.println("... und " + (predictions.size() - 10) + " weitere Zahlen.");
                break;
            }
        }
        System.out.println("----------------------------------------------");

        // Du kannst auch die Summe der Wahrscheinlichkeiten ausgeben, zur Überprüfung
        double sum = predictions.stream().mapToDouble(PredictionItem::probability).sum();
        System.out.printf("Gesamtsumme der Wahrscheinlichkeiten: %.6f\n", sum);
    }
}