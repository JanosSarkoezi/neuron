package com.example.sandbox.gemini.neuronal.activation;

public final class Tanh  implements ActivationFunction {

    @Override
    public double[] activate(double[] x) {
        double[] out = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            out[i] = Math.tanh(x[i]);
        }
        return out;
    }

    @Override
    public double[][] derivative(double[] x) {
        double[][] jacobi = new double[x.length][x.length];
        double[] tanhVals = activate(x);
        for (int i = 0; i < x.length; i++) {
            jacobi[i][i] = 1.0 - tanhVals[i] * tanhVals[i];
        }
        return jacobi;
    }

    @Override
    public double[] applyDerivative(double[] z, double[] errors) {
        double[] tanhVals = activate(z);
        double[] result = new double[errors.length];
        for (int i = 0; i < errors.length; i++) {
            result[i] = errors[i] * (1.0 - tanhVals[i] * tanhVals[i]);
        }
        return result;
    }
}
