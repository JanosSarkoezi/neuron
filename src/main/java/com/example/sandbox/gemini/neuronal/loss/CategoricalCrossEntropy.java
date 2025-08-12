package com.example.sandbox.gemini.neuronal.loss;

public class CategoricalCrossEntropy implements LossFunction {
    @Override
    public double calculateLoss(double[] predicted, double[] expected) {
        double loss = 0;
        for (int i = 0; i < predicted.length; i++) {
            // Um die Division durch Null zu vermeiden, wird ein sehr kleiner Wert (1e-15) hinzugefügt.
            loss -= expected[i] * Math.log(predicted[i] + 1e-15);
        }
        return loss;
    }

    @Override
    public double[] calculateLossGradient(double[] predicted, double[] expected) {
        double[] errors = new double[predicted.length];
        for (int i = 0; i < predicted.length; i++) {
            // Dies ist die Ableitung der Kreuzentropie in Kombination mit der Softmax-Funktion.
            // Die Softmax-Aktivierung ist entscheidend für diese einfache Ableitung.
            errors[i] = predicted[i] - expected[i];
        }
        return errors;
    }
}