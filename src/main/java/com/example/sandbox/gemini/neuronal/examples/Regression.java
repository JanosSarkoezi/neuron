package com.example.sandbox.gemini.neuronal.examples;

import com.example.sandbox.gemini.neuronal.Layer;
import com.example.sandbox.gemini.neuronal.NeuralNetwork;
import com.example.sandbox.gemini.neuronal.activation.ReLU;
import com.example.sandbox.gemini.neuronal.loss.LossFunction;
import com.example.sandbox.gemini.neuronal.loss.MeanSquaredError;

import java.util.Random;

public class Regression {
    public static void main(String[] args) {
        // Schritt 1: Netzwerk und Verlustfunktion instanziieren
        // MSE ist eine Standard-Verlustfunktion für Regressionsprobleme
        LossFunction mse = new MeanSquaredError();
        NeuralNetwork network = new NeuralNetwork(mse);

        // Schritt 2: Netzwerkarchitektur definieren
        // Input-Layer: 1 Neuron (für den x-Wert)
        // Hidden-Layer: 4 Neuronen (für komplexere Zusammenhänge, falls nötig)
        // Output-Layer: 1 Neuron (für den y-Wert)
        network.addLayer(new Layer(4, 1, new ReLU()));
        network.addLayer(new Layer(1, 4, new ReLU()));

        // Schritt 3: Trainingsdaten generieren
        int numSamples = 1000;
        double[][] trainingInputs = new double[numSamples][1];
        double[][] trainingOutputs = new double[numSamples][1];
        Random random = new Random(42);

        for (int i = 0; i < numSamples; i++) {
            double x = random.nextDouble() * 10; // Zufallswerte von 0 bis 10
            double y = 2 * x + 1; // Die Funktion, die das Netz lernen soll
            trainingInputs[i][0] = x;
            trainingOutputs[i][0] = y;
        }

        // Schritt 4: Trainingsparameter festlegen
        int epochs = 500;
        double learningRate = 0.001;

        System.out.println("--- Starte das Training ---");
        for (int i = 0; i < epochs; i++) {
            double epochLoss = 0.0;
            for (int j = 0; j < trainingInputs.length; j++) {
                network.train(trainingInputs[j], trainingOutputs[j], learningRate);

                double[] prediction = network.predict(trainingInputs[j]);
                epochLoss += mse.calculateLoss(prediction, trainingOutputs[j]);
            }

            // Ausgabe des Verlusts alle 50 Epochen zur Überwachung
            if ((i + 1) % 50 == 0) {
                System.out.printf("Epoche %d, Durchschnittlicher Verlust: %.6f%n", (i + 1), epochLoss / numSamples);
            }
        }
        System.out.println("--- Training abgeschlossen ---");
        System.out.println();

        // Schritt 5: Testen des trainierten Netzwerks mit neuen Werten
        System.out.println("--- Teste Vorhersagen ---");
        double[] testInputs = {5.0, 7.5, 9.9};
        for (double testX : testInputs) {
            double[] prediction = network.predict(new double[]{testX});
            double expectedY = 2 * testX + 1;
            System.out.printf("Input x = %.2f -> Vorhersage y = %.4f (Erwartet: %.2f)%n",
                    testX, prediction[0], expectedY);
        }
    }
}