package com.example.sandbox.gemini.neuronal;

import com.example.sandbox.gemini.neuronal.activation.ReLU;
import com.example.sandbox.gemini.neuronal.activation.Sigmoid;
import com.example.sandbox.gemini.neuronal.loss.BinaryCrossEntropy;
import com.example.sandbox.gemini.neuronal.loss.LossFunction;
import com.example.sandbox.gemini.neuronal.loss.MeanSquaredError;

public class Main {
    public static void main(String[] args) {
        // Schritt 1: Netzwerk und Verlustfunktion instanziieren
        LossFunction mse = new MeanSquaredError();
        NeuralNetwork network = new NeuralNetwork(mse);

        // Annahme: Daten laden
        double[][] trainingInputs = { {0.0, 0.0}, {0.0, 1.0}, {1.0, 0.0}, {1.0, 1.0} };
        double[][] trainingOutputs = { {0.0}, {1.0}, {1.0}, {0.0} };

        // Schritt 2: Layer hinzufügen
        network.addLayer(new Layer(2, 2, new ReLU())); // Hidden Layer
        network.addLayer(new Layer(1, 2, new Sigmoid())); // Output Layer

        // Schritt 3: Trainingsparameter festlegen
        int epochs = 50000;
        double learningRate = 0.01;

        // Schritt 4: Trainingsschleifen erstellen
        for (int i = 0; i < epochs; i++) {
            double epochLoss = 0.0;
            for (int j = 0; j < trainingInputs.length; j++) {
                double[] input = trainingInputs[j];
                double[] expectedOutput = trainingOutputs[j];

                // Die train-Methode für einen einzelnen Datensatz aufrufen
                network.train(input, expectedOutput, learningRate);

                // Optional: Verlust berechnen, um den Trainingsfortschritt zu überwachen
                double[] prediction = network.predict(input);
                epochLoss += mse.calculateLoss(prediction, expectedOutput);
            }
            if ((i + 1) % 100 == 0) {
                System.out.println("Epoche " + (i + 1) + ", Verlust: " + (epochLoss / trainingInputs.length));
            }
        }

        System.out.println("\nErgebnisse nach dem Training:");
        for (int i = 0; i < trainingInputs.length; i++) {
            double[] input = trainingInputs[i];
            double[] output = network.predict(input);
            System.out.printf("Eingabe: [%.0f, %.0f] -> Vorhergesagte Ausgabe: %.4f (Erwartet: %.0f)%n",
                    input[0], input[1], output[0], trainingOutputs[i][0]);
        }
    }
}