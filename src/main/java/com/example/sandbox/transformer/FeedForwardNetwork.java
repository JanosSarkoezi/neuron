package com.example.sandbox.transformer;

import com.example.sandbox.neuron.Layer;
import com.example.sandbox.neuron.Matrix;
import com.example.sandbox.neuron.activation.ReLU;
import com.example.sandbox.neuron.optimizer.AdamOptimizer;

public class FeedForwardNetwork {
    private final Layer hiddenLayer;
    private final Layer outputLayer;

    public FeedForwardNetwork(int d_model, int d_ff) {
        // d_model: Dimension des Eingabe-Vektors
        // d_ff: Dimension der versteckten Schicht (üblicherweise 4 * d_model)

        // Die erste Schicht
        this.hiddenLayer = Layer.builder()
                .withSize(d_model, d_ff)
                .withActivation(ReLU::new) // ReLU ist Standard für die erste Schicht
                .withOptimizerBySize(AdamOptimizer::new)
                .withWeightInitializer(Matrix::random)
                .withBiasInitializer((rows, cols, rand) -> Matrix.zeros(rows, 1))
                .build();

        // Die zweite, finale Schicht
        this.outputLayer = Layer.builder()
                .withSize(d_ff, d_model)
                .withActivation(null) // Keine Aktivierung, da es eine lineare Transformation ist
                .withOptimizerBySize(AdamOptimizer::new)
                .withWeightInitializer(Matrix::random)
                .withBiasInitializer((rows, cols, rand) -> Matrix.zeros(rows, 1))
                .build();
    }

    public Matrix forward(Matrix input) {
        Matrix hiddenOutput = hiddenLayer.feedForward(input);
        return outputLayer.feedForward(hiddenOutput);
    }

    // Sie könnten hier eine Methode für das Training hinzufügen,
    // die die Backpropagation durch beide Layer koordiniert.
    // Das vereinfacht die Logik in den EncoderLayer- und DecoderLayer-Klassen.
}