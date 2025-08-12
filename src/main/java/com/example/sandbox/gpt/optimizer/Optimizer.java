package com.example.sandbox.gpt.optimizer;

import com.example.sandbox.gpt.Layer;
import com.example.sandbox.gpt.Matrix;

public interface Optimizer {
    void update(Layer layer, Matrix dW, Matrix dB, double learningRate);
}