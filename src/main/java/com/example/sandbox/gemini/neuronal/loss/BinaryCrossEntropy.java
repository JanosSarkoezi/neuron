package com.example.sandbox.gemini.neuronal.loss;

public class BinaryCrossEntropy implements LossFunction {

    @Override
    public double calculateLoss(double[] predicted, double[] expected) {
        double epsilon = 1e-15; // verhindert log(0)
        double loss = 0;
        for (int i = 0; i < predicted.length; i++) {
            double p = Math.min(Math.max(predicted[i], epsilon), 1 - epsilon);
            loss += - (expected[i] * Math.log(p) + (1 - expected[i]) * Math.log(1 - p));
        }
        return loss / predicted.length;
    }

    @Override
    public double[] calculateLossGradient(double[] predicted, double[] expected) {
        double epsilon = 1e-15;
        double[] errors = new double[predicted.length];
        for (int i = 0; i < predicted.length; i++) {
            double p = Math.min(Math.max(predicted[i], epsilon), 1 - epsilon);
            errors[i] = (p - expected[i]) / (p * (1 - p));
        }
        return errors;
    }
}