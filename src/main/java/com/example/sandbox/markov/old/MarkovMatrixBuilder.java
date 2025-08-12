package com.example.sandbox.markov.old;

import com.example.sandbox.lotto.old.DataReader;
import com.example.sandbox.lotto.old.Ziehung;
import com.example.sandbox.neuron.Matrix;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MarkovMatrixBuilder {
    public static void main(String[] args) throws IOException, URISyntaxException {
        ClassLoader classLoader = LottoMarkov.class.getClassLoader();
        URI uri = Objects.requireNonNull(classLoader.getResource("lotto_6aus49_ab_02.12.2000.txt")).toURI();
//        URI uri = Objects.requireNonNull(classLoader.getResource("test.txt")).toURI();
        Path filePath = Path.of(uri);

        List<Ziehung> ziehungen = DataReader.readData(filePath);
        List<Ziehung> filtered = ziehungen.stream()
                .filter(ziehung -> ziehung.tagDerZiehung().isAfter(LocalDate.of(2023, 1, 1)))
                .collect(Collectors.toList());

//        Optional<Ziehung> removed = ListUtil.removeLastAndSave(filtered, 1);
        List<List<Integer>> historischeZiehungen = filtered.stream()
                .map(Ziehung::zahlen)
                .toList();

        Matrix markovMatrix = buildMarkovMatrix(historischeZiehungen, 49);
        List<Integer> currentState = historischeZiehungen.getLast();
        Matrix stateDistribution = MarkovPredictor.createStateDistribution(currentState, 49);

        Matrix nextDayPrediction = MarkovPredictor.predictNextState(markovMatrix, stateDistribution);
        List<Integer> prediction = MarkovPredictor.getTopNPredictions(nextDayPrediction, 6).stream()
                .map(StateProbability::state)
                .map(x -> x + 1)
                .sorted()
                .toList();

//        removed.map(Ziehung::zahlen).ifPresent(System.out::println);
        System.out.println(currentState);
        System.out.println(prediction);
    }

    public static Matrix buildMarkovMatrix(List<List<Integer>> observations,
                                           int totalStates) {
        double[][] transitionCounts = new double[totalStates][totalStates];

        for (int day = 0; day < observations.size() - 1; day++) {
            List<Integer> currentDay = observations.get(day);
            List<Integer> nextDay = observations.get(day + 1);

            for (Integer currentState : currentDay) {
                for (Integer nextState : nextDay) {
                    transitionCounts[currentState - 1][nextState - 1] += 1.0;
                }
            }
        }

        return normalizeToMarkovMatrix(transitionCounts);
    }

    // KORRIGIERT: Jetzt Zeilen-Normalisierung
    private static Matrix normalizeToMarkovMatrix(double[][] countMatrix) {
        int size = countMatrix.length;
        double[][] markovData = new double[size][size];

        for (int i = 0; i < size; i++) { // Für jede ZEILE (von-Zustand)
            double rowSum = 0.0;

            // Berechne Gesamtsumme der Zeile i
            for (int j = 0; j < size; j++) {
                rowSum += countMatrix[i][j];
            }

            // Normalisiere die ZEILE
            if (rowSum > 0) {
                for (int j = 0; j < size; j++) {
                    markovData[i][j] = countMatrix[i][j] / rowSum;
                }
            } else {
                // Gleichmäßige Verteilung bei keine Übergängen
                double uniformProb = 1.0 / size;
                for (int j = 0; j < size; j++) {
                    markovData[i][j] = uniformProb;
                }
            }
        }

        return new Matrix(size, size, markovData);
    }
}
