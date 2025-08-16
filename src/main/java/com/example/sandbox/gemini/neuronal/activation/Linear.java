package com.example.sandbox.gemini.neuronal.activation;

public final class Linear implements ActivationFunction {
    @Override
    public double[] activate(double[] inputs) {
        return inputs;
    }

    @Override
    public double[][] derivative(double[] inputs) {
        double[][] identity = new double[inputs.length][inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            identity[i][i] = 1.0;
        }
        return identity;
    }

    @Override
    public double[] applyDerivative(double[] z, double[] errors) {
        return errors.clone(); // Ableitung = 1 → Fehler unverändert
    }
}