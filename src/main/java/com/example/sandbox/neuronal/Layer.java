package com.example.sandbox.neuronal;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Layer {
    private List<List<Double>> weights;
    private List<Double> bias;
    private List<Double> outputs; // Speichert die Ausgaben des Vorwärtsdurchlaufs
    private List<Double> deltas;  // Speichert die Fehler (Deltas) für die Backpropagation

    public Layer(int inputSize, int outputSize) {
        this.weights = randomMatrix(inputSize, outputSize);
        this.bias = randomArray(outputSize);
    }

    private List<Double> randomArray(int size) {
        Random random = new Random();
        return IntStream.range(0, size)
                .mapToDouble(i -> random.nextDouble() - 0.5) // Zufällige Werte zwischen -0.5 und 0.5
                .boxed()
                .collect(Collectors.toList());
    }

    private List<List<Double>> randomMatrix(int rows, int cols) {
        return IntStream.range(0, rows)
                .mapToObj(r -> randomArray(cols))
                .collect(Collectors.toList());
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private double sigmoidDerivative(double y) {
        return y * (1 - y);
    }

    // Vorwärtsdurchlauf (Forward Propagation)
    public List<Double> forward(List<Double> inputs) {
        this.outputs = IntStream.range(0, bias.size())
                .mapToObj(j -> {
                    double weightedSum = IntStream.range(0, inputs.size())
                            .mapToDouble(i -> inputs.get(i) * weights.get(i).get(j))
                            .sum();
                    return sigmoid(weightedSum + bias.get(j));
                })
                .collect(Collectors.toList());
        return this.outputs;
    }

    // Rückwärtsdurchlauf (Backpropagation) für die Ausgabeschicht
    public void backward(List<Double> outputs, List<Double> targets, List<Double> inputs, double learningRate) {
        // 1. Deltas der Ausgabeschicht berechnen
        this.deltas = IntStream.range(0, outputs.size())
                .mapToDouble(i -> (targets.get(i) - outputs.get(i)) * sigmoidDerivative(outputs.get(i)))
                .boxed()
                .collect(Collectors.toList());

        // 2. Gewichte und Bias aktualisieren
        updateWeightsAndBias(inputs, learningRate);
    }

    // Rückwärtsdurchlauf (Backpropagation) für versteckte Schichten
    public void backward(List<Double> outputs, List<Double> nextLayerDeltas, List<Double> inputs, double learningRate, List<List<Double>> nextLayerWeights) {
        // 1. Deltas der versteckten Schicht berechnen
        this.deltas = IntStream.range(0, outputs.size())
                .mapToDouble(j -> {
                    double weightedDeltaSum = IntStream.range(0, nextLayerDeltas.size())
                            .mapToDouble(k -> nextLayerDeltas.get(k) * nextLayerWeights.get(j).get(k))
                            .sum();
                    return weightedDeltaSum * sigmoidDerivative(outputs.get(j));
                })
                .boxed()
                .collect(Collectors.toList());

        // 2. Gewichte und Bias aktualisieren
        updateWeightsAndBias(inputs, learningRate);
    }

    // Methode zur Aktualisierung von Gewichten und Bias
    private void updateWeightsAndBias(List<Double> inputs, double learningRate) {
        IntStream.range(0, inputs.size())
                .forEach(i -> IntStream.range(0, bias.size())
                        .forEach(j -> {
                            double deltaWeight = inputs.get(i) * deltas.get(j) * learningRate;
                            weights.get(i).set(j, weights.get(i).get(j) + deltaWeight);
                        }));

        IntStream.range(0, bias.size())
                .forEach(j -> {
                    double deltaBias = deltas.get(j) * learningRate;
                    bias.set(j, bias.get(j) + deltaBias);
                });
    }

    // Getter für Deltas und Gewichte (für die nächste Schicht)
    public List<Double> getDeltas() { return deltas; }
    public List<List<Double>> getWeights() { return weights; }
}