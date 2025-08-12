package com.example.sandbox.gemini.neuronal;

import com.example.sandbox.gemini.neuronal.loss.LossFunction;
import com.example.sandbox.gemini.neuronal.util.MatrixOperations;

import java.util.ArrayList;
import java.util.List;

public class NeuralNetwork {
    private final List<Layer> layers;
    private final LossFunction lossFunction;

    public NeuralNetwork(LossFunction lossFunction) {
        this.layers = new ArrayList<>();
        this.lossFunction = lossFunction;
    }

    public void addLayer(Layer layer) {
        this.layers.add(layer);
    }

    public double[] predict(double[] input) {
        double[] currentInput = input;
        for (Layer layer : layers) {
            currentInput = layer.feedForward(currentInput);
        }
        return currentInput;
    }

    public void train(double[] input, double[] expectedOutput, double learningRate) {
        // Schritt 1: Feed-Forward-Pass
        double[] prediction = predict(input);

        // Schritt 2: Den Gradienten der Verlustfunktion berechnen
        double[] currentErrors = lossFunction.calculateLossGradient(prediction, expectedOutput);

        // Schritt 3: Backpropagation-Pass starten
        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer currentLayer = layers.get(i);

            // 3a. Fehler der aktuellen Schicht berechnen
            double[][] afDerivatives = currentLayer.getActivationFunction().derivative(currentLayer.getLastOutputSums());
            double[] propagatedErrors = MatrixOperations.multiply(afDerivatives, currentErrors);

            // 3b. Inputs für die Gewichtsanpassung
            double[] inputsToCurrentLayer = (i == 0) ? input : layers.get(i - 1).getLastOutputs();

            // 3c. Gewichte des aktuellen Layers aktualisieren
            for (int j = 0; j < currentLayer.getNeurons().size(); j++) {
                Neuron neuron = currentLayer.getNeurons().get(j);
                neuron.updateWeights(inputsToCurrentLayer, propagatedErrors[j], learningRate);
            }

            // 3d. Fehler für den vorherigen Layer berechnen
            // Hier nutzen wir die neue, optimierte Methode
            if (i > 0) {
                double[][] weightsMatrix = currentLayer.getWeightsMatrix();
                currentErrors = MatrixOperations.multiplyWithTranspose(weightsMatrix, propagatedErrors);
            }
        }
    }
}