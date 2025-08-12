package com.example.sandbox.gemini.neuronal.activation;

/**
 * Von Google eingeführt, ähnlich wie Sigmoid × x.
 */
public final class Swish implements ActivationFunction {

    @Override
    public double[] activate(double[] x) {
        double[] out = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            double sig = 1.0 / (1.0 + Math.exp(-x[i]));
            out[i] = x[i] * sig;
        }
        return out;
    }

    @Override
    public double[][] derivative(double[] x) {
        double[][] jacobi = new double[x.length][x.length];
        for (int i = 0; i < x.length; i++) {
            double sig = 1.0 / (1.0 + Math.exp(-x[i]));
            jacobi[i][i] = sig + x[i] * sig * (1 - sig);
        }
        return jacobi;
    }

    @Override
    public double[] applyDerivative(double[] z, double[] errors) {
        double[] result = new double[errors.length];
        for (int i = 0; i < errors.length; i++) {
            double sig = 1.0 / (1.0 + Math.exp(-z[i]));
            result[i] = errors[i] * (sig + z[i] * sig * (1 - sig));
        }
        return result;
    }
}
