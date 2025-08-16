package com.example.sandbox.gemini.neuronal.activation;

/**
 * Wird u. a. in BERT, Transformer-Netzen genutzt.
 */
public final class GELU implements ActivationFunction {

    @Override
    public double[] activate(double[] x) {
        double[] out = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            out[i] = 0.5 * x[i] * (1.0 + Math.tanh(
                    Math.sqrt(2.0 / Math.PI) * (x[i] + 0.044715 * Math.pow(x[i], 3))
            ));
        }
        return out;
    }

    @Override
    public double[][] derivative(double[] x) {
        // Näherung der Ableitung
        double[][] jacobi = new double[x.length][x.length];
        for (int i = 0; i < x.length; i++) {
            double tanhTerm = Math.tanh(Math.sqrt(2.0 / Math.PI) * (x[i] + 0.044715 * Math.pow(x[i], 3)));
            double sech2Term = 1 - tanhTerm * tanhTerm;
            double inner = Math.sqrt(2.0 / Math.PI) * (1 + 3 * 0.044715 * x[i] * x[i]);
            jacobi[i][i] = 0.5 * (1 + tanhTerm) + 0.5 * x[i] * sech2Term * inner;
        }
        return jacobi;
    }

    @Override
    public double[] applyDerivative(double[] z, double[] errors) {
        double[] result = new double[errors.length];
        double[][] jacobi = derivative(z); // Kann optimiert werden
        for (int i = 0; i < errors.length; i++) {
            result[i] = errors[i] * jacobi[i][i];
        }
        return result;
    }
}
