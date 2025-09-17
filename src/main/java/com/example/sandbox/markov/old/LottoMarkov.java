package com.example.sandbox.markov.old;

import com.example.sandbox.lotto.old.DataReader;
import com.example.sandbox.lotto.old.ListUtil;
import com.example.sandbox.lotto.old.Ziehung;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LottoMarkov {

    record Transition(int from, int to) {}

    public static void main(String[] args) throws IOException, URISyntaxException {
        ClassLoader classLoader = LottoMarkov.class.getClassLoader();
        URI uri = Objects.requireNonNull(classLoader.getResource("lotto_6aus49_ab_02.12.2000.txt")).toURI();
//        URI uri = Objects.requireNonNull(classLoader.getResource("test.txt")).toURI();
        Path filePath = Path.of(uri);

        List<Ziehung> ziehungen = DataReader.readData(filePath);
        List<Ziehung> filtered = ziehungen.stream()
                .filter(ziehung -> ziehung.tagDerZiehung().isAfter(LocalDate.of(2025, 1, 1)))
                .collect(Collectors.toList());

        Optional<Ziehung> removed = ListUtil.removeLastAndSave(filtered, 1);
        List<List<Integer>> historischeZiehungen = filtered.stream()
                .map(Ziehung::zahlen)
                .toList();

        // Übergänge zählen
        Map<Integer, Map<Integer, Long>> uebergangsMatrixMap = uebergansMatrix(historischeZiehungen);

        // Wahrscheinlichkeiten berechnen
        Map<Integer, Map<Integer, Double>> wahrscheinlichkeitMatrixMap = warscheinlichkeitsMatrix(uebergangsMatrixMap);

        Set<Integer> zahlenSet = new HashSet<>(removed.orElse(filtered.getLast()).zahlen());
        Map<Integer, Double> currentState = IntStream.rangeClosed(1, 49)
                .boxed()
                .collect(Collectors.toMap(
                        i -> i,
                        i -> zahlenSet.contains(i) ? 1.0 : 0.0
                ));

        Map<Integer, Double> nextState = nextState(wahrscheinlichkeitMatrixMap, currentState);

        System.out.println("Nächster Zustandsvektor:");
        nextState.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(6)
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.printf("Zahl %d: %.4f%n", e.getKey(), e.getValue()));

        System.out.println(removed.orElse(filtered.getLast()).zahlen());

    }

    private static Map<Integer, Double> nextState(Map<Integer, Map<Integer, Double>> wahrscheinlichkeitMatrixMap, Map<Integer, Double> currentState) {
        return wahrscheinlichkeitMatrixMap.keySet().stream()
                // Zielzustände j
                .collect(Collectors.toMap(
                        j -> j,
                        j -> currentState.entrySet().stream()
                                // alle Startzustände i summieren
                                .mapToDouble(e -> e.getValue() *
                                        wahrscheinlichkeitMatrixMap
                                                .getOrDefault(e.getKey(), Collections.emptyMap())
                                                .getOrDefault(j, 0.0))
                                .sum()
                ));
    }

    private static Map<Integer, Map<Integer, Double>> warscheinlichkeitsMatrix(Map<Integer, Map<Integer, Long>> uebergangsMatrixMap) {
        return uebergangsMatrixMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            long sum = e.getValue().values().stream().mapToLong(Long::longValue).sum();
                            return e.getValue().entrySet().stream()
                                    .collect(Collectors.toMap(
                                            Map.Entry::getKey,
                                            v -> (double) v.getValue() / sum
                                    ));
                        }
                ));
    }

    private static Map<Integer, Map<Integer, Long>> uebergansMatrix(List<List<Integer>> historischeZiehungen) {
        return historischeZiehungen.stream()
                .map(z -> z.stream().sorted().toList())
                .flatMap(sorted -> IntStream.range(0, sorted.size() - 1)
                        .mapToObj(i -> new Transition(sorted.get(i), sorted.get(i + 1))))
                .collect(Collectors.groupingBy(
                        Transition::from,
                        Collectors.groupingBy(
                                Transition::to,
                                Collectors.counting()
                        )
                ));
    }
}
