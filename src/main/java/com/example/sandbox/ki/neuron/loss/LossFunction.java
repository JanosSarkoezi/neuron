package com.example.sandbox.ki.neuron.loss;

import com.example.sandbox.ki.neuron.Matrix;

public interface LossFunction {
    double loss(Matrix expected, Matrix actual);
    Matrix derivative(Matrix expected, Matrix actual);
}