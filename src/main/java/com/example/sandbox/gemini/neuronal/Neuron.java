package com.example.sandbox.gemini.neuronal;

import java.util.Random;

public class Neuron {
    private final double[] weights;
    private double bias;

    public enum InitType { HE, XAVIER, UNIFORM }

    public Neuron(int numInputs, Random rng, InitType initType) {
        this.weights = new double[numInputs];
        if (rng == null) rng = new Random(); // Standard ohne festen Seed
        initializeWeightsAndBias(rng, initType);
    }

    private void initializeWeightsAndBias(Random rng, InitType initType) {
        switch (initType) {
            case HE     -> initHe(rng);
            case XAVIER -> initXavier(rng);
            case UNIFORM-> initUniform(rng);
        }
    }

    // --- Initialisierungsstrategien ---
    private void initHe(Random rng) {
        double stddev = Math.sqrt(2.0 / weights.length);
        for (int i = 0; i < weights.length; i++) {
            weights[i] = rng.nextGaussian() * stddev;
        }
        bias = 0.0;
    }

    private void initXavier(Random rng) {
        double stddev = Math.sqrt(1.0 / weights.length);
        for (int i = 0; i < weights.length; i++) {
            weights[i] = rng.nextGaussian() * stddev;
        }
        bias = 0.0;
    }

    private void initUniform(Random rng) {
        for (int i = 0; i < weights.length; i++) {
            weights[i] = rng.nextDouble() - 0.5;
        }
        bias = rng.nextDouble() - 0.5;
    }

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

    public void updateWeights(double[] input, double error, double learningRate) {
        for (int i = 0; i < weights.length; i++) {
            weights[i] -= learningRate * error * input[i];
        }
        bias -= learningRate * error;
    }

    public double[] getWeights() {
        return weights;
    }

    public double getBias() {
        return bias;
    }
}
