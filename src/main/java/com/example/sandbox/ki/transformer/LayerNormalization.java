package com.example.sandbox.ki.transformer;

import com.example.sandbox.ki.neuron.Matrix;

public class LayerNormalization {
    private final Matrix gamma;
    private final Matrix beta;

    // Speicher für den Rückwärtspass
    private Matrix lastInput;
    private Matrix lastMean;
    private Matrix lastStdDevInv; // 1 / sqrt(variance + epsilon)

    public LayerNormalization(int d_model) {
        // Gamma wird mit Einsen initialisiert, Beta mit Nullen
        // Anmerkung: Ihre Matrix-Klasse hat noch keine 'ones'-Methode,
        // Sie müssten diese hinzufügen.
        this.gamma = Matrix.ones(d_model, 1);
        this.beta = Matrix.zeros(d_model, 1);
    }

    /**
     * Führt den Vorwärtspass der Layer-Normalisierung aus.
     *
     * @param input Die Eingabematrix.
     * @return Die normalisierte und transformierte Ausgabematrix.
     */
    public Matrix forward(Matrix input) {
        this.lastInput = input;

        // Berechnung von Mittelwert und Varianz für jede Zeile
        Matrix mean = input.meanByRow();
        Matrix variance = input.subtract(mean).pow(2).meanByRow();

        // Speichern für den Rückwärtspass
        this.lastMean = mean;

        // Inverses der Standardabweichung mit einem kleinen Epsilon zur numerischen Stabilität
        double epsilon = 1e-5;
        this.lastStdDevInv = variance.add(epsilon).pow(-0.5);

        // Normalisierung
        Matrix normalized = input.subtract(mean).hadamard(lastStdDevInv);

        // Skalierung und Verschiebung (gamma * normalized + beta)
        return gamma.hadamard(normalized).add(beta);
    }

    /**
     * Führt den Rückwärtspass der Layer-Normalisierung aus.
     *
     * @param delta Der Gradient, der von der nachfolgenden Schicht kommt.
     * @return Das Delta, das an die vorherige Schicht weitergegeben wird.
     */
    public Matrix backward(Matrix delta) {
        Matrix d_normalized = delta.hadamard(this.gamma);

        // Gradienten für gamma und beta berechnen
        Matrix d_beta = delta.sumByRow();
        Matrix d_gamma = delta.hadamard(lastInput.subtract(lastMean)).hadamard(lastStdDevInv).sumByRow();

        // Propagieren des Deltas zurück
        int N = lastInput.rows();
        Matrix term1 = d_normalized.hadamard(lastStdDevInv);
        Matrix term2 = lastInput.subtract(lastMean).hadamard(lastStdDevInv.pow(3)).hadamard(d_normalized.sumByRow()).divide(N);
        Matrix term3 = d_normalized.hadamard(lastStdDevInv).sumByRow().divide(N);

        Matrix d_input = term1.subtract(term2).subtract(term3);

        // Hier würden Sie die Parameter (gamma, beta) mit einem Optimizer updaten
        // updateParameters(d_gamma, d_beta);

        return d_input;
    }
}