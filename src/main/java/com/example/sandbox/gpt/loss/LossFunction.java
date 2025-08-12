package com.example.sandbox.gpt.loss;

import com.example.sandbox.gpt.Matrix;

public interface LossFunction {
    double loss(Matrix expected, Matrix actual);
    Matrix derivative(Matrix expected, Matrix actual);
}