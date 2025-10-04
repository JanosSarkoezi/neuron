package com.example.sandbox.ki.neuron;

import com.example.sandbox.ki.neuron.activation.Sigmoid;
import com.example.sandbox.ki.neuron.loss.MeanSquaredError;
import com.example.sandbox.ki.neuron.optimizer.AdamOptimizer;

public class NNExample {
    public static void main(String[] args) {
        // --- Netzwerk aufbauen ---
        NeuralNetwork nn = new NeuralNetwork(MeanSquaredError::new);

        // Hidden Layer: 2 Eingaben -> 2 Neuronen
        nn.addLayer(Layer.builder()
                .withSize(2, 2)
                .withActivation(Sigmoid::new)
                .withOptimizerBySize(AdamOptimizer::new)
                .withWeightInitializer(Matrix::random)
                .withBiasInitializer(Matrix::random)
                .withSeed(12)
                .build());

        // Output Layer: 2 Eingaben -> 1 Neuron
        nn.addLayer(Layer.builder()
                .withSize(2, 1)
                .withActivation(Sigmoid::new)
                .withOptimizerBySize(AdamOptimizer::new)
                .withWeightInitializer(Matrix::random)
                .withBiasInitializer(Matrix::random)
                .withSeed(12)
                .build());

        // --- XOR Trainingsdaten ---
        double[][] inputs = {
                {0, 0},
                {0, 1},
                {1, 0},
                {1, 1}
        };
        double[][] outputs = {
                {0},
                {1},
                {1},
                {0}
        };

        // --- Training ---
        int epochs = 10000;
        double lr = 0.1;

        for (int epoch = 0; epoch < epochs; epoch++) {
            double epochLoss = 0.0;
            for (int i = 0; i < inputs.length; i++) {
                Matrix input = new Matrix(inputs[i]);   // Spaltenvektor
                Matrix expected = new Matrix(outputs[i]);
                nn.train(input, expected, lr);

                Matrix predict = nn.predict(input);
                epochLoss = nn.getLossFunction().loss(predict, expected);
            }

            if ((epoch + 1) % 100 == 0) {
                System.out.println("Epoche " + (epoch + 1) + ", Verlust: " + (epochLoss / inputs.length));
            }
        }

        // --- Testen ---
        System.out.println("XOR Vorhersagen:");
        for (int i = 0; i < inputs.length; i++) {
            Matrix input = new Matrix(inputs[i]);
            Matrix output = nn.predict(input);
            System.out.printf("%d XOR %d = %.4f%n",
                    (int)inputs[i][0], (int)inputs[i][1], output.data()[0][0]);
        }
    }
}
