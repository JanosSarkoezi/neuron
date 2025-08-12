package com.example.sandbox.gemini.neuronal.loss;

public interface LossFunction {
    double calculateLoss(double[] predicted, double[] expected);
    double[] calculateLossGradient(double[] predicted, double[] expected);
}