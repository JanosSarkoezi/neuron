package com.example.sandbox.neuron.activation;

import com.example.sandbox.neuron.Matrix;

public class Softmax implements ActivationFunction {
    @Override
    public Matrix apply(Matrix z) {
        // Exponentiation jedes Elements
        Matrix expZ = z.map(Math::exp);
        // Summe aller Elemente
        double sum = 0.0;
        for (int i = 0; i < expZ.rows(); i++) {
            sum += expZ.data()[i][0];
        }
        // Normalisierung
        final double s = sum;
        return expZ.map(x -> x / s);
    }

    @Override
    public Matrix derivative(Matrix z) {
        Matrix sigma = apply(z);
        int K = sigma.rows();

        // Schritt 1: Erstelle die Diagonalmatrix diag(sigma)
        double[][] diagData = new double[K][K];
        for (int i = 0; i < K; i++) {
            diagData[i][i] = sigma.data()[i][0];
        }
        Matrix diagSigma = new Matrix(K, K, diagData);

        // Schritt 2: Berechne das äußere Produkt sigma * sigma^T
        Matrix outerProduct = sigma.dot(sigma.transpose());

        // Schritt 3: Berechne die Differenz diag(sigma) - sigma * sigma^T
        return diagSigma.subtract(outerProduct);
    }
}
