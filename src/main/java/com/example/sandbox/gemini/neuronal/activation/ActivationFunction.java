package com.example.sandbox.gemini.neuronal.activation;

public sealed interface ActivationFunction permits ReLU, Sigmoid, Softmax, Linear {
    double[] activate(double[] x);
    double[][] derivative(double[] x);
}