package com.example.sandbox.gemini.neuronal;

import java.util.Random;

public class Neuron {
    private double[] weights;
    private double bias;

    // Statische, reproduzierbare Zufallsinstanz für konsistente Initialisierung
    private static final Random random = new Random(12);

    public Neuron(int numInputs) {
        this.weights = new double[numInputs];
        // Gewichte und Bias mit kleinen Zufallswerten initialisieren,
        // z.B. aus einer normalisierten Verteilung
        initializeWeightsAndBias();
    }

    private void initializeWeightsAndBias() {
        // Hier könnte eine Logik zur Initialisierung der Gewichte stehen,
        // z.B. He-Initialisierung oder Xavier-Initialisierung.
        // Für dieses Beispiel verwenden wir einfache Zufallswerte.
        for (int i = 0; i < weights.length; i++) {
            weights[i] = random.nextDouble() - 0.5;
        }
        this.bias = random.nextDouble() - 0.5;
    }

    /**
     * Führt die gewichtete Summe der Inputs durch.
     * @param inputs Der Input-Vektor vom vorherigen Layer.
     * @return Die gewichtete Summe.
     */
    public double calculateWeightedSum(double[] inputs) {
        if (inputs.length != weights.length) {
            throw new IllegalArgumentException("Input-Länge stimmt nicht mit der Anzahl der Gewichte überein.");
        }

        double sum = bias;
        for (int i = 0; i < inputs.length; i++) {
            sum += inputs[i] * weights[i];
        }
        return sum;
    }

    /**
     * Passt die Gewichte und den Bias an, basierend auf dem Fehler und der Lernrate.
     * @param input Der Input-Vektor, der im Feed-Forward-Pass verwendet wurde.
     * @param error Der Fehlergradient für dieses Neuron.
     * @param learningRate Die Lernrate.
     */
    public void updateWeights(double[] input, double error, double learningRate) {
        for (int i = 0; i < weights.length; i++) {
            weights[i] -= learningRate * error * input[i];
        }
        bias -= learningRate * error;
    }

    // --- Getter-Methode ---
    public double[] getWeights() {
        return weights;
    }
}