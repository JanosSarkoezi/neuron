package com.example.sandbox.ki.neuron.optimizer;

import com.example.sandbox.ki.neuron.Layer;
import com.example.sandbox.ki.neuron.Matrix;

public interface Optimizer {
    void update(Layer layer, Matrix dW, Matrix dB, double learningRate);
}