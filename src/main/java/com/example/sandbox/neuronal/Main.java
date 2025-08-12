package com.example.sandbox.neuronal;

import com.example.sandbox.DataReader;
import com.example.sandbox.Ziehung;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        ClassLoader classLoader = Main.class.getClassLoader();
//        URI uri = Objects.requireNonNull(classLoader.getResource("lotto_6aus49_ab_02.12.2000.txt")).toURI();
        URI uri = Objects.requireNonNull(classLoader.getResource("test.txt")).toURI();
        Path filePath = Path.of(uri);

        try {
            List<Ziehung> ziehungen = DataReader.readData(filePath);
            List<Ziehung> filtered = ziehungen.stream()
                    .filter(ziehung -> ziehung.tagDerZiehung().isAfter(LocalDate.of(2024, 1, 1)))
                    .collect(Collectors.toList());

            // filtered.forEach(System.out::println);
            Ziehung removed = filtered.remove(filtered.size() - 1);

            double min = 1.0;
            double max = 49.0;

            // 1. Gesamten Datensatz normalisieren
            List<List<Double>> sequences = filtered.stream()
                    .map(Ziehung::zahlen)
                    .map(zahlenListe -> zahlenListe.stream()
                            .map(zahl -> DataNormalizer.normalize(zahl, min, max)) // 'min' und 'max' aus dem gesamten Datensatz
                            .toList())
                    .toList();

            // 2. Normalisierte Daten in Inputs und Targets aufteilen
            int numPairs = sequences.size() - 1;
            List<List<Double>> inputs = new ArrayList<>();
            List<List<Double>> targets = new ArrayList<>();

            for (int i = 0; i < numPairs; i++) {
                inputs.add(sequences.get(i));
                targets.add(sequences.get(i + 1));
            }

            // 3. Inputs und Targets in Trainings- und Testsets aufteilen
            int trainSize = (int) (numPairs * 0.8);
            var trainX = inputs.subList(0, trainSize);
            var trainY = targets.subList(0, trainSize);
            var testX = inputs.subList(trainSize, numPairs);
            var testY = targets.subList(trainSize, numPairs);

            NeuralNetwork neuralNetwork = new NeuralNetwork(6, 16, 6);
            neuralNetwork.train(trainX, trainY, 10000, 0.005);

//            double mse = IntStream.range(0, testX.size())
//                    .mapToDouble(i -> {
//                        var pred = neuralNetwork.predict(testX.get(i));
//                        return IntStream.range(0, pred.size())
//                                .mapToDouble(j -> Math.pow(pred.get(j) - testY.get(i).get(j), 2))
//                                .sum();
//                    })
//                    .sum();
//            mse /= (testX.size() * 6);
//            System.out.printf("\nTest-MSE: %.6f%n", mse);

            List<Integer> zahlen = filtered.getLast().zahlen();
            List<Double> normalized = zahlen.stream()
                    .map(zahl -> DataNormalizer.normalize(zahl, min, max))
                    .toList();
            var pred = neuralNetwork.predict(normalized);
            List<Integer> gewinnzahlen = pred.stream()
                    .map(zahl -> DataNormalizer.denormalize(zahl, min, max))
                    .toList();

            System.out.println("Prediction: " + gewinnzahlen);
            System.out.println("Actual: " + removed.zahlen());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
