package com.example.sandbox.markov.old;

import com.example.sandbox.neuron.Matrix;

import java.util.ArrayList;
import java.util.List;

public class MarkovPredictor {

    // Vorhersage für den nächsten Zustand
    public static Matrix predictNextState(Matrix markovMatrix, Matrix currentState) {
        // Matrix * Rechts (Spaltenvektor) = Neuer Spaltenvektor
        return markovMatrix.dot(currentState);
    }

    // Vorhersage für n Schritte in die Zukunft
    public static Matrix predictNSteps(Matrix markovMatrix, Matrix currentState, int steps) {
        Matrix result = currentState;
        for (int i = 0; i < steps; i++) {
            result = markovMatrix.dot(result);
        }
        return result;
    }

    // Effizientere Version mit Matrix-Potenz
    public static Matrix predictNStepsEfficient(Matrix markovMatrix, Matrix currentState, int steps) {
        Matrix poweredMatrix = markovMatrix;
        for (int i = 1; i < steps; i++) {
            poweredMatrix = poweredMatrix.dot(markovMatrix);
        }
        return poweredMatrix.dot(currentState);
    }

    public static Matrix createStateDistribution(List<Integer> observations, int totalStates) {
        double[] distribution = new double[totalStates];
        for (int state : observations) {
            distribution[state - 1] += 1.0;
        }
        // Normalisiere zu Wahrscheinlichkeiten
        for (int i = 0; i < totalStates; i++) {
            distribution[i] /= observations.size();
        }
        return new Matrix(distribution);
    }

    public static List<Double> matrixToProbabilityList(Matrix prediction) {
        List<Double> probabilities = new ArrayList<>();
        for (int i = 0; i < prediction.rows(); i++) {
            probabilities.add(prediction.get(i, 0));
        }
        return probabilities;
    }

    public static List<StateProbability> getTopNPredictions(Matrix prediction, int n) {
        List<StateProbability> allPredictions = getSortedPredictions(prediction);
        return allPredictions.subList(0, Math.min(n, allPredictions.size()));
    }

    public static List<StateProbability> getSortedPredictions(Matrix prediction) {
        List<StateProbability> result = matrixToStateProbabilityList(prediction);

        // Sortiere absteigend nach Wahrscheinlichkeit
        result.sort((a, b) -> Double.compare(b.probability(), a.probability()));

        return result;
    }

    public static List<StateProbability> matrixToStateProbabilityList(Matrix prediction) {
        List<StateProbability> result = new ArrayList<>();
        for (int i = 0; i < prediction.rows(); i++) {
            result.add(new StateProbability(i, prediction.get(i, 0)));
        }
        return result;
    }

}