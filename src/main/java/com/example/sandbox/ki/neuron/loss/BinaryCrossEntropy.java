package com.example.sandbox.ki.neuron.loss;

import com.example.sandbox.ki.neuron.Matrix;

public class BinaryCrossEntropy implements LossFunction {

    @Override
    public double loss(Matrix expected, Matrix actual) {
        double sum = 0.0;
        int m = expected.rows();
        int n = expected.cols();

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double y = expected.data()[i][j];
                double p = actual.data()[i][j];

                // numerische Stabilisierung
                p = Math.max(1e-12, Math.min(1 - 1e-12, p));

                sum += -(y * Math.log(p) + (1 - y) * Math.log(1 - p));
            }
        }
        return sum / (m * n); // Mittelwert über alle Einträge
    }

    @Override
    public Matrix derivative(Matrix expected, Matrix actual) {
        double[][] grad = new double[expected.rows()][expected.cols()];

        for (int i = 0; i < expected.rows(); i++) {
            for (int j = 0; j < expected.cols(); j++) {
                double y = expected.data()[i][j];
                double p = actual.data()[i][j];

                // numerische Stabilisierung
                p = Math.max(1e-12, Math.min(1 - 1e-12, p));

                grad[i][j] = -(y / p) + (1 - y) / (1 - p);
            }
        }
        return new Matrix(expected.rows(), expected.cols(), grad);
    }
}
