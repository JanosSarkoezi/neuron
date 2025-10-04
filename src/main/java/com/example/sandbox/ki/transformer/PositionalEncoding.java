package com.example.sandbox.ki.transformer;

import com.example.sandbox.ki.neuron.Matrix;

public class PositionalEncoding {
    private final int wordLength;
    private final int d_model; // Die Dimension des Embeddings
    private final Matrix encodingMatrix;

    public PositionalEncoding(int wordLength, int d_model) {
        this.wordLength = wordLength;
        this.d_model = d_model;

        // Initialisieren der Positionskodierungsmatrix
        double[][] encodingData = new double[wordLength][d_model];
        for (int i = 0; i < wordLength; i++) {
            for (int j = 0; j < d_model; j++) {
                // Hier koennen Sie eine feste Kodierung verwenden,
                // z.B. sinus/cosinus-Funktionen, oder eine einfache lineare Kodierung.
                // Echte Transformer nutzen:
                // PE(pos, 2i) = sin(pos / 10000^(2i/d_model))
                // PE(pos, 2i+1) = cos(pos / 10000^(2i/d_model))

                if (j % 2 == 0) {
                    encodingData[i][j] = Math.sin(i / Math.pow(10000, (double) j / d_model));
                } else {
                    encodingData[i][j] = Math.cos(i / Math.pow(10000, (double) (j - 1) / d_model));
                }
            }
        }
        this.encodingMatrix = new Matrix(wordLength, d_model, encodingData);
    }

    public Matrix apply(int[] inputSequence) {
        if (inputSequence.length != wordLength) {
            throw new IllegalArgumentException("Eingabesequenz muss Laenge " + wordLength + " haben.");
        }

        // Token Embedding (ohne separate Klasse)
        // Wandelt die durchnummerierten Buchstaben in eine Matrix um
        double[][] tokenData = new double[wordLength][d_model];
        for (int i = 0; i < wordLength; i++) {
            int tokenIndex = inputSequence[i];
            if (tokenIndex >= 0 && tokenIndex < d_model) {
                tokenData[i][tokenIndex] = 1.0; // One-Hot-Kodierung
            }
        }
        Matrix tokenMatrix = new Matrix(wordLength, d_model, tokenData);

        // Hinzufügen der Positionskodierung zur Token-Matrix
        return tokenMatrix.add(this.encodingMatrix);
    }
}