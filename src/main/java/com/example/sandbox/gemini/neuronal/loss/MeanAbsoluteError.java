package com.example.sandbox.gemini.neuronal.loss;

public class MeanAbsoluteError implements LossFunction {
    @Override
    public double calculateLoss(double[] predicted, double[] expected) {
        double sumOfAbsoluteErrors = 0;
        for (int i = 0; i < predicted.length; i++) {
            sumOfAbsoluteErrors += Math.abs(expected[i] - predicted[i]);
        }
        return sumOfAbsoluteErrors / predicted.length;
    }

    @Override
    public double[] calculateLossGradient(double[] predicted, double[] expected) {
        double[] errors = new double[predicted.length];
        for (int i = 0; i < predicted.length; i++) {
            // Die Ableitung des MAE ist 1 oder -1, je nach Vorzeichen der Differenz.
            double diff = predicted[i] - expected[i];
            if (diff > 0) {
                errors[i] = 1;
            } else if (diff < 0) {
                errors[i] = -1;
            } else {
                errors[i] = 0;
            }
        }
        return errors;
    }
}