package com.example.sandbox.gemini.neuronal.activation;

/**
 * Wie ReLU, aber mit kleinem negativen Faktor statt harter Nullsetzung – verhindert "Dead ReLUs".
 */
public final class LeakyReLU implements ActivationFunction {
    private final double alpha;

    public LeakyReLU() {
        this(0.01); // Standardwert
    }

    public LeakyReLU(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public double[] activate(double[] x) {
        double[] out = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            out[i] = (x[i] > 0) ? x[i] : alpha * x[i];
        }
        return out;
    }

    @Override
    public double[][] derivative(double[] x) {
        double[][] jacobi = new double[x.length][x.length];
        for (int i = 0; i < x.length; i++) {
            jacobi[i][i] = (x[i] > 0) ? 1.0 : alpha;
        }
        return jacobi;
    }

    @Override
    public double[] applyDerivative(double[] z, double[] errors) {
        double[] result = new double[errors.length];
        for (int i = 0; i < errors.length; i++) {
            result[i] = errors[i] * ((z[i] > 0) ? 1.0 : alpha);
        }
        return result;
    }
}
