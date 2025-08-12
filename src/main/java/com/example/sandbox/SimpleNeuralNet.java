package com.example.sandbox;

import java.util.Random;

public class SimpleNeuralNet {
    private final double[][] weightsInputHidden;
    private final double[][] weightsHiddenOutput;
    private final double[] hiddenBias;
    private final double[] outputBias;
    private final Random random = new Random();

    public SimpleNeuralNet(int inputSize, int hiddenSize, int outputSize) {
        weightsInputHidden = new double[inputSize][hiddenSize];
        weightsHiddenOutput = new double[hiddenSize][outputSize];
        hiddenBias = new double[hiddenSize];
        outputBias = new double[outputSize];

        initRandom(weightsInputHidden);
        initRandom(weightsHiddenOutput);
        initRandom(hiddenBias);
        initRandom(outputBias);
    }

    private void initRandom(double[] arr) {
        for (int i = 0; i < arr.length; i++)
            arr[i] = random.nextDouble() - 0.5;
    }

    private void initRandom(double[][] arr) {
        for (int i = 0; i < arr.length; i++)
            initRandom(arr[i]);
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private double sigmoidDerivative(double x) {
        return x * (1 - x);
    }

    public double[] predict(double[] input) {
        // Hidden layer
        double[] hidden = new double[hiddenBias.length];
        for (int j = 0; j < hidden.length; j++) {
            hidden[j] = hiddenBias[j];
            for (int i = 0; i < input.length; i++)
                hidden[j] += input[i] * weightsInputHidden[i][j];
            hidden[j] = sigmoid(hidden[j]);
        }

        // Output layer
        double[] output = new double[outputBias.length];
        for (int k = 0; k < output.length; k++) {
            output[k] = outputBias[k];
            for (int j = 0; j < hidden.length; j++)
                output[k] += hidden[j] * weightsHiddenOutput[j][k];
            output[k] = sigmoid(output[k]);
        }
        return output;
    }

    public void train(double[][] inputs, double[][] targets, int epochs, double learningRate) {
        for (int epoch = 0; epoch < epochs; epoch++) {
            for (int sample = 0; sample < inputs.length; sample++) {
                // Forward pass
                double[] hidden = new double[hiddenBias.length];
                for (int j = 0; j < hidden.length; j++) {
                    hidden[j] = hiddenBias[j];
                    for (int i = 0; i < inputs[sample].length; i++)
                        hidden[j] += inputs[sample][i] * weightsInputHidden[i][j];
                    hidden[j] = sigmoid(hidden[j]);
                }

                double[] outputs = new double[outputBias.length];
                for (int k = 0; k < outputs.length; k++) {
                    outputs[k] = outputBias[k];
                    for (int j = 0; j < hidden.length; j++)
                        outputs[k] += hidden[j] * weightsHiddenOutput[j][k];
                    outputs[k] = sigmoid(outputs[k]);
                }

                // Output error
                double[] outputErrors = new double[outputs.length];
                for (int k = 0; k < outputs.length; k++)
                    outputErrors[k] = targets[sample][k] - outputs[k];

                // Backpropagation: Output to Hidden
                double[] outputGrad = new double[outputs.length];
                for (int k = 0; k < outputs.length; k++)
                    outputGrad[k] = outputErrors[k] * sigmoidDerivative(outputs[k]) * learningRate;

                for (int j = 0; j < hidden.length; j++) {
                    for (int k = 0; k < outputs.length; k++)
                        weightsHiddenOutput[j][k] += hidden[j] * outputGrad[k];
                }
                for (int k = 0; k < outputs.length; k++)
                    outputBias[k] += outputGrad[k];

                // Hidden layer error
                double[] hiddenErrors = new double[hidden.length];
                for (int j = 0; j < hidden.length; j++) {
                    double error = 0;
                    for (int k = 0; k < outputs.length; k++)
                        error += outputErrors[k] * weightsHiddenOutput[j][k];
                    hiddenErrors[j] = error;
                }

                // Hidden gradient
                double[] hiddenGrad = new double[hidden.length];
                for (int j = 0; j < hidden.length; j++)
                    hiddenGrad[j] = hiddenErrors[j] * sigmoidDerivative(hidden[j]) * learningRate;

                for (int i = 0; i < inputs[sample].length; i++) {
                    for (int j = 0; j < hidden.length; j++)
                        weightsInputHidden[i][j] += inputs[sample][i] * hiddenGrad[j];
                }
                for (int j = 0; j < hidden.length; j++)
                    hiddenBias[j] += hiddenGrad[j];
            }
        }
    }

    public static void main(String[] args) {
        // XOR-Beispiel
        double[][] inputs = {
                {0, 0},
                {0, 1},
                {1, 0},
                {1, 1}
        };
        double[][] targets = {
                {0},
                {1},
                {1},
                {0}
        };

        var nn = new SimpleNeuralNet(2, 8, 1);
        nn.train(inputs, targets, 50000, 0.1);

        for (double[] input : inputs) {
            double[] output = nn.predict(input);
            System.out.printf("%s -> %.4f%n", java.util.Arrays.toString(input), output[0]);
        }
    }
}
