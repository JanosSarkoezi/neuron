package com.example.sandbox.transformer;

import com.example.sandbox.neuron.Matrix;
import com.example.sandbox.neuron.activation.ActivationFunction;

public class LayerNormalization implements ActivationFunction {
    private final double epsilon = 1e-6;
    private Matrix gamma; // Skalierungs-Parameter
    private Matrix beta;  // Verschiebungs-Parameter

    public LayerNormalization(int inputSize) {
        // Initialisierung von Gamma und Beta, die gelernt werden
        this.gamma = Matrix.ones(inputSize, 1);
        this.beta = Matrix.zeros(inputSize, 1);
    }

    @Override
    public Matrix apply(Matrix input) {
        // Berechne Mittelwert und Varianz
        double mean = input.mean();
        double variance = input.subtract(mean).square().mean();

        // Normalisierung
        Matrix normalized = input.subtract(mean).multiply(1.0 / Math.sqrt(variance + epsilon));

        // Skalierung und Verschiebung
        return normalized.hadamard(gamma).add(beta);
    }

    @Override
    public Matrix derivative(Matrix z) {
        // Die Ableitung ist komplexer, hier nur eine vereinfachte Version
        // Für das Training müssten die Gradienten für Gamma und Beta berechnet werden.
        return Matrix.ones(z.rows(), z.cols());
    }
}