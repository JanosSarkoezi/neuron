package com.example.sandbox.gemini.neuronal.activation;

/**
 * Glättet den negativen Bereich – oft stabiler als ReLU.
 */
public final class ELU implements ActivationFunction {
    private final double alpha;

    public ELU() {
        this(1.0);
    }

    public ELU(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public double[] activate(double[] x) {
        double[] out = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            out[i] = (x[i] >= 0) ? x[i] : alpha * (Math.exp(x[i]) - 1);
        }
        return out;
    }

    @Override
    public double[][] derivative(double[] x) {
        double[][] jacobi = new double[x.length][x.length];
        for (int i = 0; i < x.length; i++) {
            jacobi[i][i] = (x[i] >= 0) ? 1.0 : alpha * Math.exp(x[i]);
        }
        return jacobi;
    }

    @Override
    public double[] applyDerivative(double[] z, double[] errors) {
        double[] result = new double[errors.length];
        for (int i = 0; i < errors.length; i++) {
            result[i] = errors[i] * ((z[i] >= 0) ? 1.0 : alpha * Math.exp(z[i]));
        }
        return result;
    }
}
