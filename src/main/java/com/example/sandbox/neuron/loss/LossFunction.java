package com.example.sandbox.neuron.loss;

import com.example.sandbox.neuron.Matrix;

public interface LossFunction {
    double loss(Matrix expected, Matrix actual);
    Matrix derivative(Matrix expected, Matrix actual);
}