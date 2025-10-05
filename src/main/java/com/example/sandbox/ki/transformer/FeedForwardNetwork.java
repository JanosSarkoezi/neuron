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
                .withActivation(() -> {
                    return new com.example.sandbox.ki.neuron.activation.ActivationFunction() {
                        @Override
                        public Matrix apply(Matrix z) {
                            return z.copy(); // Return a copy to avoid mutable state issues
                        }

                        @Override
                        public Matrix derivative(Matrix z) {
                            return Matrix.ones(z.rows(), z.cols());
                        }
                    };
                }) // Linear
                .withOptimizerBySize(AdamOptimizer::new)
                .withWeightInitializer(Matrix::xavier)
                .withBiasInitializer((rows, cols, rand) -> Matrix.zeros(rows, 1))
                .build();
    }

    /**
     * Package-private constructor for testing purposes (Dependency Injection).
     */
    FeedForwardNetwork(Layer hiddenLayer, Layer outputLayer) {
        this.hiddenLayer = hiddenLayer;
        this.outputLayer = outputLayer;
    }

    public Matrix forward(Matrix input) {
        // Transpose input to fit the Layer convention (features, samples)
        Matrix hiddenInput = input.transpose();
        Matrix hiddenOutput = hiddenLayer.feedForward(hiddenInput);
        Matrix finalOutput = outputLayer.feedForward(hiddenOutput);
        // Transpose output back to Transformer convention (samples, features)
        return finalOutput.transpose();
    }

    /**
     * Führt den Rückwärtspass durch das Feed-Forward-Netzwerk aus.
     *
     * @param delta    Die Gradienten, die von der nachfolgenden Schicht kommen.
     * @param ffnInput Die ursprüngliche Eingabe in das Feed-Forward-Netzwerk.
     * @return Das Delta, das an die vorherige Schicht weitergegeben wird.
     */
    public Matrix backward(Matrix delta, Matrix ffnInput) {
        // Transpose incoming delta to fit Layer convention
        Matrix transposedDelta = delta.transpose();

        // 1. Backward pass through the output layer
        Matrix hiddenOutput = hiddenLayer.getLastA(); // This is already in (features, samples) format
        Matrix deltaOutputLayer = outputLayer.backward(transposedDelta, hiddenOutput);

        // 2. Backward pass through the hidden layer
        // The input to the hidden layer was the transposed original input
        Matrix transposedFfnInput = ffnInput.transpose();
        Matrix deltaHiddenLayer = hiddenLayer.backward(deltaOutputLayer, transposedFfnInput);

        // Transpose the final delta back to fit Transformer convention
        return deltaHiddenLayer.transpose();
    }
}