package com.example.sandbox.gemini.neuronal.activation;

public final class Sigmoid implements ActivationFunction {

    @Override
    public double[] activate(double[] x) {
        double[] outputs = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            outputs[i] = 1.0 / (1.0 + Math.exp(-x[i]));
        }
        return outputs;
    }

    @Override
    public double[][] derivative(double[] x) {
        double[][] jacobian = new double[x.length][x.length];
        double[] activatedOutputs = activate(x);

        for (int i = 0; i < x.length; i++) {
            // Die Ableitung der Sigmoid-Funktion ist f(x) * (1 - f(x))
            double derivativeValue = activatedOutputs[i] * (1.0 - activatedOutputs[i]);
            jacobian[i][i] = derivativeValue;
        }
        return jacobian;
    }

    @Override
    public double[] applyDerivative(double[] z, double[] errors) {
        double[] sig = activate(z);
        double[] result = new double[errors.length];
        for (int i = 0; i < errors.length; i++) {
            result[i] = errors[i] * sig[i] * (1.0 - sig[i]);
        }
        return result;
    }
}