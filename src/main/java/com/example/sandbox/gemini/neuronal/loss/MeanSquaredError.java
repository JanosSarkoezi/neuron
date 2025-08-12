package com.example.sandbox.gemini.neuronal.loss;

public class MeanSquaredError implements LossFunction {
    @Override
    public double calculateLoss(double[] predicted, double[] expected) {
        double sumOfSquaredErrors = 0;
        for (int i = 0; i < predicted.length; i++) {
            sumOfSquaredErrors += Math.pow(expected[i] - predicted[i], 2);
        }
        return sumOfSquaredErrors / predicted.length;
    }

    @Override
    public double[] calculateLossGradient(double[] predicted, double[] expected) {
        double[] errors = new double[predicted.length];
        for (int i = 0; i < predicted.length; i++) {
            // Die Ableitung des MSE ist 2 * (y_pred - y_true), hier in der Form für Backprop
            errors[i] = predicted[i] - expected[i];
        }
        return errors;
    }
}