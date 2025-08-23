package com.example.sandbox.transformer;

import com.example.sandbox.neuron.Matrix;
import com.example.sandbox.neuron.activation.Softmax;

public class MultiHeadAttention {
    private final int d_model;
    private final int numHeads;
    private final int d_k;
    private final Softmax softmax;

    private final Matrix Wq, Wk, Wv, Wo; // Gewichtsmatrizen

    // ✅ Speicherung der Zwischenergebnisse für den Rückwärtspass
    // Diese werden aus dem Vorwärtspass gespeichert.
    private Matrix Q_proj, K_proj, V_proj;
    private Matrix attentionScores;
    private Matrix attentionWeights;
    private Matrix attentionOutput;

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

    /**
     * Führt den Vorwärtspass aus und speichert Zwischenergebnisse.
     */
    public Matrix forward(Matrix query, Matrix key, Matrix value) {
        // Lineare Projektionen
        this.Q_proj = query.dot(Wq);
        this.K_proj = key.dot(Wk);
        this.V_proj = value.dot(Wv);

        // Skalierte Punktprodukt-Aufmerksamkeit
        this.attentionScores = Q_proj.dot(K_proj.transpose()).multiply(1.0 / Math.sqrt(d_k));
        this.attentionWeights = softmax.apply(attentionScores);
        this.attentionOutput = attentionWeights.dot(V_proj);

        // Lineare Projektion zurück in die ursprüngliche Dimension
        return attentionOutput.dot(Wo);
    }

    /**
     * ✅ Korrigierte Methode: Führt den Rückwärtspass aus und berechnet die Gradienten.
     */
    public Matrix backward(Matrix delta, Matrix query, Matrix key, Matrix value) {
        // Rückwärtspass der letzten linearen Projektion (Wo)
        Matrix dWo = this.attentionOutput.transpose().dot(delta);
        Matrix dOutput = delta.dot(Wo.transpose());

        // Rückwärtspass der Matrixmultiplikation mit V
        Matrix dV_proj = attentionWeights.transpose().dot(dOutput);
        Matrix dAttentionWeights = dOutput.dot(V_proj.transpose());

        // Rückwärtspass des Softmax
        Matrix dAttentionScores = softmax.derivative(attentionScores).hadamard(dAttentionWeights);

        // Rückwärtspass der skalierten Punktprodukt-Operation
        Matrix dQ_proj = dAttentionScores.dot(K_proj).multiply(1.0 / Math.sqrt(d_k));
        Matrix dK_proj = dAttentionScores.transpose().dot(Q_proj).multiply(1.0 / Math.sqrt(d_k));

        // Rückwärtspass der linearen Projektionen (Wq, Wk, Wv)
        Matrix dWq = query.transpose().dot(dQ_proj);
        Matrix dWk = key.transpose().dot(dK_proj);
        Matrix dWv = value.transpose().dot(dV_proj);

        // Der Gesamt-Delta für die vorhergehende Schicht
        Matrix dQ = dQ_proj.dot(Wq.transpose());
        Matrix dK = dK_proj.dot(Wk.transpose());
        Matrix dV = dV_proj.dot(Wv.transpose());

        return dQ.add(dK).add(dV);
    }
}