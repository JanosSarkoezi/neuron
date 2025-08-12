package com.example.sandbox.gpt.activation;

import com.example.sandbox.gpt.Matrix;

public interface ActivationFunction {
    Matrix apply(Matrix z);
    Matrix derivative(Matrix z);
}
