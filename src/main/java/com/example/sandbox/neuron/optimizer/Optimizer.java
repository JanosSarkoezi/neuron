package com.example.sandbox.neuron.optimizer;

import com.example.sandbox.neuron.Layer;
import com.example.sandbox.neuron.Matrix;

public interface Optimizer {
    void update(Layer layer, Matrix dW, Matrix dB, double learningRate);
}