package com.example.sandbox.transformer;

import com.example.sandbox.neuron.Matrix;
import com.example.sandbox.neuron.activation.Softmax;

public class MultiHeadAttention {
    private final int d_model;
    private final int numHeads;
    private final int d_k;
    private final Softmax softmax;

    private final Matrix Wq, Wk, Wv, Wo; // Gewichtsmatrizen

    public MultiHeadAttention(int d_model, int numHeads) {
        this.d_model = d_model;
        this.numHeads = numHeads;
        this.d_k = d_model / numHeads;
        this.softmax = new Softmax();

        // Initialisierung der Gewichtsmatrizen
        this.Wq = Matrix.random(d_model, d_model);
        this.Wk = Matrix.random(d_model, d_model);
        this.Wv = Matrix.random(d_model, d_model);
        this.Wo = Matrix.random(d_model, d_model);
    }

    public Matrix forward(Matrix query, Matrix key, Matrix value) {
        // Lineare Projektionen für jeden Kopf
        Matrix Q = query.dot(Wq);
        Matrix K = key.dot(Wk);
        Matrix V = value.dot(Wv);

        // Die Matrizen in "Köpfe" aufteilen (vereinfacht)
        // Normalerweise würde man hier Matrizen neu anordnen und die Köpfe parallel verarbeiten.
        // Für diese Implementierung führen wir die Operation auf den vollen Matrizen aus.

        // Skalierte Punktprodukt-Aufmerksamkeit
        Matrix attentionScores = Q.dot(K.transpose()).multiply(1.0 / Math.sqrt(d_k));
        Matrix attentionWeights = softmax.apply(attentionScores);
        Matrix attentionOutput = attentionWeights.dot(V);

        // Lineare Projektion zurück in die ursprüngliche Dimension
        return attentionOutput.dot(Wo);
    }
}