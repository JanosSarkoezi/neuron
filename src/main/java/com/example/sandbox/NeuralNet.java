package com.example.sandbox;

import java.util.*;
import java.util.random.RandomGenerator;

public class NeuralNet {
    record Layer(double[][] weights, double[] bias) {}

    private final Layer inputHidden;
    private final Layer hiddenOutput;
    private final RandomGenerator rng = RandomGenerator.of("L64X128MixRandom");

    public NeuralNet(int inputSize, int hiddenSize, int outputSize) {
        inputHidden = new Layer(randomMatrix(inputSize, hiddenSize), randomArray(hiddenSize));
        hiddenOutput = new Layer(randomMatrix(hiddenSize, outputSize), randomArray(outputSize));
    }

    private double[] randomArray(int size) {
        return rng.doubles(size, -0.5, 0.5).toArray();
    }

    private double[][] randomMatrix(int rows, int cols) {
        double[][] m = new double[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                m[i][j] = rng.nextDouble(-0.5, 0.5);
        return m;
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private double sigmoidDerivative(double y) {
        return y * (1 - y);
    }

    public double[] predict(double[] input) {
        var hidden = activate(input, inputHidden);
        return activate(hidden, hiddenOutput);
    }

    private double[] activate(double[] input, Layer layer) {
        var out = new double[layer.bias().length];
        for (int j = 0; j < out.length; j++) {
            double sum = layer.bias()[j];
            for (int i = 0; i < input.length; i++)
                sum += input[i] * layer.weights()[i][j];
            out[j] = sigmoid(sum);
        }
        return out;
    }

    public void train(double[][] inputs, double[][] targets, int epochs, double lr) {
        for (int e = 0; e < epochs; e++) {
            for (int idx = 0; idx < inputs.length; idx++) {
                var input = inputs[idx];
                var target = targets[idx];

                var hidden = activate(input, inputHidden);
                var output = activate(hidden, hiddenOutput);

                var outputErrors = new double[output.length];
                for (int k = 0; k < output.length; k++)
                    outputErrors[k] = target[k] - output[k];

                var outputGrad = new double[output.length];
                for (int k = 0; k < output.length; k++)
                    outputGrad[k] = outputErrors[k] * sigmoidDerivative(output[k]) * lr;

                for (int j = 0; j < hidden.length; j++)
                    for (int k = 0; k < output.length; k++)
                        hiddenOutput.weights()[j][k] += hidden[j] * outputGrad[k];
                for (int k = 0; k < output.length; k++)
                    hiddenOutput.bias()[k] += outputGrad[k];

                var hiddenErrors = new double[hidden.length];
                for (int j = 0; j < hidden.length; j++)
                    for (int k = 0; k < output.length; k++)
                        hiddenErrors[j] += outputErrors[k] * hiddenOutput.weights()[j][k];

                var hiddenGrad = new double[hidden.length];
                for (int j = 0; j < hidden.length; j++)
                    hiddenGrad[j] = hiddenErrors[j] * sigmoidDerivative(hidden[j]) * lr;

                for (int i = 0; i < input.length; i++)
                    for (int j = 0; j < hidden.length; j++)
                        inputHidden.weights()[i][j] += input[i] * hiddenGrad[j];
                for (int j = 0; j < hidden.length; j++)
                    inputHidden.bias()[j] += hiddenGrad[j];
            }
        }
    }

    // ---------- Hilfsmethoden ----------
    private static double normalize(int val) {
        return (val - 1) / 49.0;
    }

    private static int denormalize(double val) {
        int num = (int) Math.round(val * 49.0 + 1);
        return Math.max(1, Math.min(50, num));
    }

    public static void main(String[] args) {
        Random rnd = new Random();
        int totalSets = 105; // 105 aufeinanderfolgende 5er-Zahlen
        int[][] sequences = new int[totalSets][5];
        for (int i = 0; i < totalSets; i++)
            for (int j = 0; j < 5; j++)
                sequences[i][j] = rnd.nextInt(50) + 1;

        // Trainingspaare erstellen
        int numPairs = totalSets - 1;
        double[][] inputs = new double[numPairs][5];
        double[][] targets = new double[numPairs][5];
        for (int i = 0; i < numPairs; i++) {
            for (int j = 0; j < 5; j++) {
                inputs[i][j] = normalize(sequences[i][j]);
                targets[i][j] = normalize(sequences[i + 1][j]);
            }
        }

        // 80-20 Split
        int trainSize = (int) (numPairs * 0.8);
        double[][] trainX = Arrays.copyOfRange(inputs, 0, trainSize);
        double[][] trainY = Arrays.copyOfRange(targets, 0, trainSize);
        double[][] testX = Arrays.copyOfRange(inputs, trainSize, numPairs);
        double[][] testY = Arrays.copyOfRange(targets, trainSize, numPairs);

        // Netz erstellen und trainieren
        var nn = new NeuralNet(5, 10, 5);
        nn.train(trainX, trainY, 3000, 0.1);

        // Testdaten auswerten
        double mse = 0;
        for (int i = 0; i < testX.length; i++) {
            double[] pred = nn.predict(testX[i]);
            for (int j = 0; j < pred.length; j++) {
                double diff = pred[j] - testY[i][j];
                mse += diff * diff;
            }
        }
        mse /= (testX.length * 5);
        System.out.printf("Test-MSE: %.6f%n", mse);

        // Vorhersage für Satz 101 (Index 100)
        double[] lastNormalized = new double[5];
        for (int j = 0; j < 5; j++)
            lastNormalized[j] = normalize(sequences[100][j]);

        double[] predictedNorm = nn.predict(lastNormalized);
        int[] predicted = new int[5];
        for (int j = 0; j < 5; j++)
            predicted[j] = denormalize(predictedNorm[j]);

        System.out.println("Satz 100: " + Arrays.toString(sequences[100]));
        System.out.println("Vorhersage Satz 101: " + Arrays.toString(predicted));
        System.out.println("Tatsächlicher Satz 101: " + Arrays.toString(sequences[101]));
    }
}
