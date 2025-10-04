package com.example.sandbox.ki.neuron.loss;

import com.example.sandbox.ki.neuron.Matrix;

public class CrossEntropyLoss implements LossFunction {
    @Override
    public double loss(Matrix expected, Matrix actual) {
        double sum = 0.0;
        for (int i = 0; i < expected.rows(); i++) {
            // Nur der erwartete Wert wird für die Summe herangezogen
            // -log(a_i)
            sum += expected.data()[i][0] * Math.log(actual.data()[i][0]);
        }
        return -sum;
    }

    @Override
    public Matrix derivative(Matrix expected, Matrix actual) {
        // Der Gradient für Cross-Entropy + Softmax ist vereinfacht
        // einfach (actual - expected)
        return actual.subtract(expected);
    }
}