package com.example.sandbox.gemini.neuronal.activation;

public final class Softmax implements ActivationFunction {

    @Override
    public double[] activate(double[] x) {
        double[] outputs = new double[x.length];
        double sumOfExponentials = 0.0;

        // Schritt 1: Summe der Exponenziale berechnen
        for (double input : x) {
            sumOfExponentials += Math.exp(input);
        }

        // Schritt 2: Jedes Element durch die Summe teilen
        for (int i = 0; i < x.length; i++) {
            outputs[i] = Math.exp(x[i]) / sumOfExponentials;
        }

        return outputs;
    }

    @Override
    public double[][] derivative(double[] x) {
        double[] activatedOutputs = activate(x);
        double[][] jacobian = new double[x.length][x.length];

        // Die Jakobimatrix füllen
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x.length; j++) {
                if (i == j) {
                    // Ableitung des i-ten Outputs nach dem i-ten Input
                    // f(x_i) * (1 - f(x_i))
                    jacobian[i][j] = activatedOutputs[i] * (1.0 - activatedOutputs[i]);
                } else {
                    // Ableitung des i-ten Outputs nach dem j-ten Input
                    // -f(x_i) * f(x_j)
                    jacobian[i][j] = -activatedOutputs[i] * activatedOutputs[j];
                }
            }
        }
        return jacobian;
    }
}