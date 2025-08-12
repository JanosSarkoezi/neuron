package com.example.sandbox.gemini.neuronal.examples;

import com.example.sandbox.gemini.neuronal.Layer;
import com.example.sandbox.gemini.neuronal.NeuralNetwork;
import com.example.sandbox.gemini.neuronal.activation.Sigmoid;
import com.example.sandbox.gemini.neuronal.loss.BinaryCrossEntropy;
import com.example.sandbox.gemini.neuronal.loss.LossFunction;

import java.util.Random;

public class XORBinaryCross {
    public static void main(String[] args) {
        // Schritt 1: Netzwerk und Verlustfunktion instanziieren
        LossFunction mse = new BinaryCrossEntropy();
        NeuralNetwork network = new NeuralNetwork(mse);

        // Trainingsdaten für XOR (Input und erwartete Ausgabe)
        double[][] trainingInputs = { {0.0, 0.0}, {0.0, 1.0}, {1.0, 0.0}, {1.0, 1.0} };
        double[][] trainingOutputs = { {0.0}, {1.0}, {1.0}, {0.0} };

        // Schritt 2: Layer hinzufügen
        Random random = new Random(12);
        network.addLayer(new Layer(2, 2, new Sigmoid(), random)); // Versteckter Layer mit 2 Neuronen
        network.addLayer(new Layer(1, 2, new Sigmoid(), random)); // Ausgabeschicht mit 1 Neuron

        // Schritt 3: Trainingsparameter festlegen
        int epochs = 2000; // Mehr Epochen für bessere Konvergenz
        double learningRate = 0.1;

        System.out.println("--- Starte das Training ---");
        for (int i = 0; i < epochs; i++) {
            for (int j = 0; j < trainingInputs.length; j++) {
                network.train(trainingInputs[j], trainingOutputs[j], learningRate);
            }
        }
        System.out.println("--- Training abgeschlossen ---");
        System.out.println();

        // Schritt 4: Vorhersagen treffen und als Kategorien ausgeben
        System.out.println("--- Ergebnisse der Klassifizierung ---");
        for (int i = 0; i < trainingInputs.length; i++) {
            double[] input = trainingInputs[i];
            double[] prediction = network.predict(input);

            // Umwandlung der numerischen Vorhersage in eine Kategorie
            String category = (prediction[0] > 0.5) ? "TRUE" : "FALSE";
            String expected = (trainingOutputs[i][0] > 0.5) ? "TRUE" : "FALSE";

            System.out.printf("Input: [%.1f, %.1f] -> Vorhersage: (%.4f) %s (Erwartet: %s)%n",
                    input[0], input[1], prediction[0], category, expected);
        }
    }
}