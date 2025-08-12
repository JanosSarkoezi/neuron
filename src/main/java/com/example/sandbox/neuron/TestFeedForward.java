package com.example.sandbox.neuron;

import com.example.sandbox.neuron.activation.Sigmoid;
import com.example.sandbox.neuron.loss.MeanSquaredError;
import com.example.sandbox.neuron.optimizer.AdamOptimizer;

public class TestFeedForward {
    public static void main(String[] args) {
        // Netzwerk mit Lernrate (irrelevant hier, da wir nur forward testen)
        NeuralNetwork nn = new NeuralNetwork(MeanSquaredError::new);

        // Hidden Layer: 2 Eingaben -> 2 Neuronen
        nn.addLayer(Layer.builder()
                .withSize(2, 2)
                .withActivation(Sigmoid::new)
                .withOptimizerBySize(AdamOptimizer::new)
                .withWeightInitializer(Matrix::random)
                .withBiasInitializer(Matrix::random)
                .build());

        // Output Layer: 2 Eingaben -> 1 Neuron
        nn.addLayer(Layer.builder()
                .withSize(2, 1)
                .withActivation(Sigmoid::new)
                .withOptimizerBySize(AdamOptimizer::new)
                .withWeightInitializer(Matrix::random)
                .withBiasInitializer(Matrix::random)
                .build());

        // Eingabevektor (z.B. [1, 0])
        Matrix input = new Matrix(new double[]{1.0, 0.0});

        // Forward Pass
        Matrix output = nn.predict(input);

        System.out.println("Input: " + input);
        System.out.println("Output: " + output);
    }
}
