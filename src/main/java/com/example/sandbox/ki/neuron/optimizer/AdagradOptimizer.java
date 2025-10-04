package com.example.sandbox.ki.neuron.optimizer;

import com.example.sandbox.ki.neuron.Layer;
import com.example.sandbox.ki.neuron.Matrix;

public class AdagradOptimizer implements Optimizer {
    private Matrix cacheW;
    private Matrix cacheB;
    private final double epsilon;

    public AdagradOptimizer(int rows, int cols, double epsilon) {
        this.epsilon = epsilon;
        this.cacheW = Matrix.zeros(rows, cols);  // ✅ Korrekt initialisiert
        this.cacheB = Matrix.zeros(rows, 1);     // ✅ Korrekt initialisiert
    }

    public AdagradOptimizer(int rows, int cols) {
        this(rows, cols, 1e-8);
    }

    @Override
    public void update(Layer layer, Matrix dW, Matrix dB, double learningRate) {
        // Elegante eine Zeile für Cache Update
        cacheW = cacheW.add(dW.square());
        cacheB = cacheB.add(dB.square());

        // Elegante eine Zeile für Parameter Update
        Matrix weightUpdate = dW.hadamard(cacheW.sqrt().add(epsilon).reciprocal())
                .multiply(-learningRate);
        Matrix biasUpdate = dB.hadamard(cacheB.sqrt().add(epsilon).reciprocal())
                .multiply(-learningRate);

        layer.setWeights(layer.getWeights().add(weightUpdate));
        layer.setBiases(layer.getBiases().add(biasUpdate));
    }
}