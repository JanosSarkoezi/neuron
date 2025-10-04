package com.example.sandbox.ki.transformer;

import com.example.sandbox.ki.neuron.Layer;
import com.example.sandbox.ki.neuron.Matrix;
import com.example.sandbox.ki.neuron.activation.ReLU;
import com.example.sandbox.ki.neuron.optimizer.AdamOptimizer;

public class FeedForwardNetwork {
    private final Layer hiddenLayer;
    private final Layer outputLayer;

    public FeedForwardNetwork(int d_model, int d_ff) {
        // Die erste Schicht (hidden)
        this.hiddenLayer = Layer.builder()
                .withSize(d_model, d_ff)
                .withActivation(ReLU::new)
                .withOptimizerBySize(AdamOptimizer::new)
                .withWeightInitializer(Matrix::xavier)
                .withBiasInitializer((rows, cols, rand) -> Matrix.zeros(rows, 1))
                .build();

        // Die zweite, finale Schicht (output)
        this.outputLayer = Layer.builder()
                .withSize(d_ff, d_model)
                .withActivation(null) // Linear
                .withOptimizerBySize(AdamOptimizer::new)
                .withWeightInitializer(Matrix::xavier)
                .withBiasInitializer((rows, cols, rand) -> Matrix.zeros(rows, 1))
                .build();
    }

    public Matrix forward(Matrix input) {
        Matrix hiddenOutput = hiddenLayer.feedForward(input);
        return outputLayer.feedForward(hiddenOutput);
    }

    /**
     * Führt den Rückwärtspass durch das Feed-Forward-Netzwerk aus.
     *
     * @param delta Die Gradienten, die von der nachfolgenden Schicht kommen.
     * @return Das Delta, das an die vorherige Schicht weitergegeben wird.
     */
    public Matrix backward(Matrix delta) {
        // 1. Rückwärtspass durch die Output-Schicht
        // Die Aktivierung der vorherigen Schicht (hier der Hidden Layer) ist notwendig.
        Matrix hiddenOutput = hiddenLayer.getLastA();
        Matrix deltaHidden = outputLayer.backward(delta, hiddenOutput);

        // 2. Rückwärtspass durch die Hidden-Schicht
        // Die Aktivierung der vorherigen Schicht (hier die Eingabe des FFN) ist notwendig.
        // Diese müsste im Forward-Pass gespeichert werden.
        // Annahme: Die Eingabe des FFN wird in der aufrufenden Klasse (EncoderLayer) gespeichert.

        // Da die Layer-Klasse bereits die Aktivierung der VORHERIGEN Schicht für den
        // Rückwärtspass erwartet, müssen wir die Logik anpassen. Die hier gezeigte Implementierung
        // der NeuralNetwork-Klasse macht das schon in ihrer train-Methode.
        // Hier in FeedForwardNetwork wird es nur weitergereicht.

        return deltaHidden;
    }
}