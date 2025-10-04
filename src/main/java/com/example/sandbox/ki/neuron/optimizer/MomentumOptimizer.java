package com.example.sandbox.ki.neuron.optimizer;

import com.example.sandbox.ki.neuron.Layer;
import com.example.sandbox.ki.neuron.Matrix;

public class MomentumOptimizer implements Optimizer {
    private Matrix v_w;
    private Matrix v_b;

    private final double momentum = 0.9;

    public MomentumOptimizer(int inputSize, int outputSize) {
        // Initialisierung der Momentum-Variablen im Konstruktor
        this.v_w = Matrix.zeros(outputSize, inputSize);
        this.v_b = Matrix.zeros(outputSize, 1);
    }

    @Override
    public void update(Layer layer, Matrix dW, Matrix dB, double learningRate) {
        // Aktualisierung des Momentums für Gewichte und Biases
        this.v_w = v_w.multiply(momentum).add(dW.multiply(learningRate));
        this.v_b = v_b.multiply(momentum).add(dB.multiply(learningRate));

        // Aktualisierung der Gewichte und Biases
        layer.setWeights(layer.getWeights().subtract(v_w));
        layer.setBiases(layer.getBiases().subtract(v_b));
    }
}