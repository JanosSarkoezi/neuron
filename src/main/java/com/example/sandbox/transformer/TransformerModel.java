package com.example.sandbox.transformer;

import com.example.sandbox.neuron.Matrix;

public class TransformerModel {
    private final Encoder encoder;
    private final Decoder decoder;
    private final PositionalEncoding positionalEncoding;

    private final int d_model = 128;
    private final int numHeads = 8;
    private final int numLayers = 6;
    private final int wordLength = 5;

    public TransformerModel() {
        this.positionalEncoding = new PositionalEncoding(wordLength, d_model);
        this.encoder = new Encoder(d_model, numHeads, numLayers);
        this.decoder = new Decoder(d_model, numHeads, numLayers);
    }

    public Matrix forward(int[] inputSequence, int[] outputSequence) {
        // Encoder-Teil
        Matrix inputMatrix = positionalEncoding.apply(inputSequence);
        Matrix encoderOutput = encoder.forward(inputMatrix);

        // Decoder-Teil
        Matrix outputMatrix = positionalEncoding.apply(outputSequence);
        return decoder.forward(outputMatrix, encoderOutput);
    }
}