package com.example.sandbox.gpt;

import com.example.sandbox.gpt.activation.ActivationFunction;
import com.example.sandbox.gpt.builder.LayerBuilder;
import com.example.sandbox.gpt.optimizer.Optimizer;

public class Layer {
    // ✅ Mutable für Training
    private Matrix weights;
    private Matrix biases;

    // ✅ Immutable Konfiguration
    private final ActivationFunction activation;
    private final Optimizer optimizer;

    // ✅ Pro Forward-Pass neu berechnet (quasi immutable)
    private Matrix z;
    private Matrix a;

    public Layer(int inputSize, int outputSize, ActivationFunction activation,
                 Optimizer optimizer, Matrix initialWeights, Matrix initialBiases) {
        this.weights = initialWeights;
        this.biases = initialBiases;
        this.activation = activation;
        this.optimizer = optimizer;
    }

    // ✅ Setter notwendig
    public void setWeights(Matrix weights) { this.weights = weights; }
    public void setBiases(Matrix biases) { this.biases = biases; }

    // Getter
    public Matrix getWeights() { return weights; }
    public Matrix getBiases() { return biases; }
    public Matrix getLastZ() { return z; }
    public Matrix getLastA() { return a; }
    public ActivationFunction getActivationFunction() { return activation; }
    public Optimizer getOptimizer() { return optimizer; }

    public Matrix feedForward(Matrix input) {
        z = weights.dot(input).add(biases);
        a = activation.apply(z);
        return a;
    }

    public void updateParameters(Matrix dW, Matrix dB, double learningRate) {
        optimizer.update(this, dW, dB, learningRate);
    }

    public static LayerBuilder builder() {
        return new LayerBuilder();
    }
}
