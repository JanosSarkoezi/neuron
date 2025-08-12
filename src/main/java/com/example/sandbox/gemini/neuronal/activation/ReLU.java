package com.example.sandbox.gemini.neuronal.activation;

public final class ReLU implements ActivationFunction {

    @Override
    public double[] activate(double[] x) {
        double[] outputs = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            outputs[i] = Math.max(0, x[i]);
        }
        return outputs;
    }

    @Override
    public double[][] derivative(double[] x) {
        double[][] jacobian = new double[x.length][x.length];
        for (int i = 0; i < x.length; i++) {
            // Die Ableitung ist 1, wenn der Input > 0, sonst 0.
            // Dies ergibt eine Diagonalmatrix, da die Ableitung des i-ten Outputs
            // nur vom i-ten Input abhängt.
            jacobian[i][i] = x[i] > 0 ? 1.0 : 0.0;
        }
        return jacobian;
    }
}