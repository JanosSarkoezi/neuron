package com.example.sandbox.neuron.activation;

import com.example.sandbox.neuron.Matrix;

public interface ActivationFunction {
    Matrix apply(Matrix z);
    Matrix derivative(Matrix z);
}
