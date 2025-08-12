package com.example.sandbox.neuron.loss;

import com.example.sandbox.neuron.Matrix;

public class MeanSquaredError implements LossFunction {
    @Override
    public double loss(Matrix expected, Matrix actual) {
        double sum = 0.0;
        for (int i = 0; i < expected.rows(); i++) {
            double diff = expected.data()[i][0] - actual.data()[i][0];
            sum += diff * diff;
        }
        return sum / expected.rows();
    }

    @Override
    public Matrix derivative(Matrix expected, Matrix actual) {
        return actual.subtract(expected).multiply(2.0 / expected.rows());
    }
}
