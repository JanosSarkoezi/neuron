package com.example.sandbox.neuron.optimizer;

import com.example.sandbox.neuron.Layer;
import com.example.sandbox.neuron.Matrix;

public class SGDOptimizer implements Optimizer {
    @Override
    public void update(Layer layer, Matrix dW, Matrix dB, double learningRate) {
        Matrix weights = layer.getWeights();
        Matrix biases = layer.getBiases();

        // Update der Gewichte und Biases mit dem einfachen Gradientenabstieg
        layer.setWeights(weights.subtract(dW.multiply(learningRate)));
        layer.setBiases(biases.subtract(dB.multiply(learningRate)));
    }
}