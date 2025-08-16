package com.example.sandbox.gemini.neuronal.activation;

public sealed interface ActivationFunction permits ReLU, Sigmoid, Softmax, Linear {
    double[] activate(double[] x);
    double[][] derivative(double[] x);

    /**
     * Multipliziert den Fehler mit der Ableitung der Aktivierungsfunktion.
     * Kann optimiert sein für elementweise oder komplexe AFs.
     */
    double[] applyDerivative(double[] z, double[] errors);
}