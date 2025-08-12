package com.example.sandbox.gemini.neuronal.examples;

import com.example.sandbox.gemini.neuronal.Layer;
import com.example.sandbox.gemini.neuronal.NeuralNetwork;
import com.example.sandbox.gemini.neuronal.activation.Linear;
import com.example.sandbox.gemini.neuronal.activation.ReLU;
import com.example.sandbox.gemini.neuronal.loss.LossFunction;
import com.example.sandbox.gemini.neuronal.loss.MeanSquaredError;

import java.util.Random;

public class Trainer {
    public static void main(String[] args) {
        // Schritt 1: Netzwerk und Verlustfunktion instanziieren
        LossFunction mse = new MeanSquaredError();
        NeuralNetwork network = new NeuralNetwork(mse);

        // Schritt 2: Netzwerkarchitektur definieren
        network.addLayer(new Layer(4, 1, new ReLU()));
        network.addLayer(new Layer(1, 4, new Linear()));

        // Schritt 3: Trainings- und Validierungsdaten generieren
        int numTotalSamples = 1000;
        double[][] allInputs = new double[numTotalSamples][1];
        double[][] allOutputs = new double[numTotalSamples][1];
        Random random = new Random(42);

        for (int i = 0; i < numTotalSamples; i++) {
            double x = random.nextDouble() * 10;
            double y = 2 * x + 1;
            allInputs[i][0] = x;
            allOutputs[i][0] = y;
        }

        // Schritt 4: Aufteilung in Trainings- und Validierungsdatensätze
        int trainSize = (int) (numTotalSamples * 0.8);

        double[][] trainInputs = new double[trainSize][1];
        double[][] trainOutputs = new double[trainSize][1];
        double[][] validationInputs = new double[numTotalSamples - trainSize][1];
        double[][] validationOutputs = new double[numTotalSamples - trainSize][1];

        // Die Daten in die neuen Arrays kopieren
        System.arraycopy(allInputs, 0, trainInputs, 0, trainSize);
        System.arraycopy(allOutputs, 0, trainOutputs, 0, trainSize);
        System.arraycopy(allInputs, trainSize, validationInputs, 0, numTotalSamples - trainSize);
        System.arraycopy(allOutputs, trainSize, validationOutputs, 0, numTotalSamples - trainSize);

        // Schritt 5: Trainingsparameter festlegen
        int epochs = 500;
        double learningRate = 0.001;

        System.out.println("--- Starte das Training ---");
        for (int i = 0; i < epochs; i++) {
            double trainLoss = 0.0;
            // Trainieren nur mit dem Trainingsdatensatz
            for (int j = 0; j < trainInputs.length; j++) {
                network.train(trainInputs[j], trainOutputs[j], learningRate);
                trainLoss += mse.calculateLoss(network.predict(trainInputs[j]), trainOutputs[j]);
            }

            // Validierung nach jeder Epoche
            double validationLoss = 0.0;
            for (int j = 0; j < validationInputs.length; j++) {
                double[] prediction = network.predict(validationInputs[j]);
                validationLoss += mse.calculateLoss(prediction, validationOutputs[j]);
            }

            if ((i + 1) % 100 == 0) {
                System.out.printf("Epoche %d, Trainingsverlust: %.6f, Validierungsverlust: %.6f%n",
                        (i + 1), trainLoss / trainSize, validationLoss / validationInputs.length);
            }
        }
        System.out.println("--- Training abgeschlossen ---");

        // Schritt 6: Testen der Vorhersagen auf dem Validierungsdatensatz
        System.out.println("--- Teste Vorhersagen auf Validierungsdaten ---");
        double[] testInputs = {5.0, 7.5, 9.9, 0.0};
        for (double testX : testInputs) {
            double[] prediction = network.predict(new double[]{testX});
            double expectedY = 2 * testX + 1;
            System.out.printf("Input x = %.2f -> Vorhersage y = %.4f (Erwartet: %.2f)%n",
                    testX, prediction[0], expectedY);
        }
    }
}