package com.example.sandbox.ki.neuron.activation;

import com.example.sandbox.ki.neuron.Matrix;

public interface ActivationFunction {
    Matrix apply(Matrix z);
    Matrix derivative(Matrix z);
}
