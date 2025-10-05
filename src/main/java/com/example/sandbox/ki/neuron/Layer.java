package com.example.sandbox.ki.neuron;

import com.example.sandbox.ki.neuron.activation.ActivationFunction;
import com.example.sandbox.ki.neuron.builder.LayerBuilder;
import com.example.sandbox.ki.neuron.optimizer.Optimizer;

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
        Matrix z_unbiased = weights.dot(input);
        // Manually add bias to each column (sample) to broadcast
        for (int i = 0; i < z_unbiased.cols(); i++) {
            for (int j = 0; j < z_unbiased.rows(); j++) {
                z_unbiased.data()[j][i] += biases.data()[j][0];
            }
        }
        z = z_unbiased;
        a = activation.apply(z);
        return a;
    }

    public void updateParameters(Matrix dW, Matrix dB, double learningRate) {
        optimizer.update(this, dW, dB, learningRate);
    }

    /**
     * Führt den Rückwärtspass für diesen Layer aus.
     *
     * @param delta Die Gradienten der nachfolgenden Schicht.
     * @param aPrev Die Aktivierung der VORHERIGEN Schicht.
     * @return Das Delta, das an die vorherige Schicht weitergegeben wird.
     */
    public Matrix backward(Matrix delta, Matrix aPrev) {
        // 1. Delta mit der Ableitung der Aktivierungsfunktion multiplizieren
        Matrix activationDerivative = activation.derivative(this.z);
        Matrix deltaPostActivation = delta.hadamard(activationDerivative);

        // 2. Gradienten für Gewichte und Biases berechnen
        Matrix dW = deltaPostActivation.dot(aPrev.transpose());
        
        // Correctly calculate dB for batches by summing gradients across samples (columns)
        double[][] biasGradientData = new double[deltaPostActivation.rows()][1];
        for (int i = 0; i < deltaPostActivation.rows(); i++) {
            double sum = 0.0;
            for (int j = 0; j < deltaPostActivation.cols(); j++) {
                sum += deltaPostActivation.get(i, j);
            }
            biasGradientData[i][0] = sum;
        }
        Matrix dB = new Matrix(deltaPostActivation.rows(), 1, biasGradientData);

        // 4. Das Delta für die vorherige Schicht berechnen (BEVOR die Gewichte upgedated werden)
        Matrix deltaForPreviousLayer = this.weights.transpose().dot(deltaPostActivation);

        // 3. Parameter updaten
        updateParameters(dW, dB, 0.001); // Lernrate wird vom Trainer übergeben

        // Das vorkalkulierte Delta zurückgeben
        return deltaForPreviousLayer;
    }

    public static LayerBuilder builder() {
        return new LayerBuilder();
    }
}
