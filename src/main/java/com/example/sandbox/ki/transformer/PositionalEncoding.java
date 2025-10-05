package com.example.sandbox.ki.transformer;

import com.example.sandbox.ki.neuron.Layer;
import com.example.sandbox.ki.neuron.Matrix;

public class PositionalEncoding {
    private final int wordLength;
    private final int d_model;
    private final int vocabSize;
    private final Layer embeddingLayer;
    private final Matrix encodingMatrix;
    private int[] lastInputSequence; // Store for backward pass

    public PositionalEncoding(int wordLength, int d_model, int vocabSize, Layer embeddingLayer) {
        this.wordLength = wordLength;
        this.d_model = d_model;
        this.vocabSize = vocabSize;
        this.embeddingLayer = embeddingLayer;

        // Initialisieren der Positionskodierungsmatrix
        double[][] encodingData = new double[wordLength][d_model];
        for (int i = 0; i < wordLength; i++) {
            for (int j = 0; j < d_model; j++) {
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
        this.lastInputSequence = inputSequence;

        // Create dense embeddings from token indices
        Matrix embeddings = Matrix.zeros(d_model, wordLength);
        for (int i = 0; i < wordLength; i++) {
            int tokenIndex = inputSequence[i];

            // Create one-hot vector for the token
            double[][] oneHotData = new double[vocabSize][1];
            if (tokenIndex >= 0 && tokenIndex < vocabSize) {
                oneHotData[tokenIndex][0] = 1.0;
            }
            Matrix oneHotVector = new Matrix(vocabSize, 1, oneHotData);

            // Get dense embedding from the layer
            Matrix embeddingVector = embeddingLayer.feedForward(oneHotVector);

            // Place the resulting column vector into the embeddings matrix
            for (int j = 0; j < d_model; j++) {
                embeddings.set(j, i, embeddingVector.get(j, 0));
            }
        }

        // Transpose from (d_model, seq_len) to (seq_len, d_model) and add positional encoding
        return embeddings.transpose().add(this.encodingMatrix);
    }

    public void backward(Matrix delta) {
        // The incoming delta is for the combined (embedding + positional) matrix.
        // The gradient for the positional encoding is zero as it's not learned.
        // So, the delta applies directly to the learned embeddings.
        Matrix d_embeddings_transposed = delta;
        Matrix d_embeddings = d_embeddings_transposed.transpose();

        for (int i = 0; i < wordLength; i++) {
            // Get the gradient for the i-th embedding vector by extracting the column
            double[][] dEmbeddingColData = new double[d_model][1];
            for(int row = 0; row < d_model; row++) {
                dEmbeddingColData[row][0] = d_embeddings.get(row, i);
            }
            Matrix d_embedding_vector = new Matrix(d_model, 1, dEmbeddingColData);

            // Re-create the one-hot vector for the i-th token (as aPrev for the backward pass)
            int tokenIndex = this.lastInputSequence[i];
            double[][] oneHotData = new double[vocabSize][1];
            if (tokenIndex >= 0 && tokenIndex < vocabSize) {
                oneHotData[tokenIndex][0] = 1.0;
            }
            Matrix oneHotVector = new Matrix(vocabSize, 1, oneHotData);

            // Backpropagate through the embedding layer. This updates its weights.
            embeddingLayer.backward(d_embedding_vector, oneHotVector);
        }
    }
}