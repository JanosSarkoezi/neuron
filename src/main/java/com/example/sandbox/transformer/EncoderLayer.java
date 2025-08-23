package com.example.sandbox.transformer;

import com.example.sandbox.neuron.Matrix;

public class EncoderLayer {
    private final MultiHeadAttention selfAttention;
    private final FeedForwardNetwork feedForward;
    private final LayerNormalization norm1;
    private final LayerNormalization norm2;

    public EncoderLayer(int d_model, int numHeads) {
        this.selfAttention = new MultiHeadAttention(d_model, numHeads);
        this.feedForward = new FeedForwardNetwork(d_model, d_model * 4);
        this.norm1 = new LayerNormalization(d_model);
        this.norm2 = new LayerNormalization(d_model);
    }

    public Matrix forward(Matrix input) {
        // 1. Self-Attention mit Residuenverbindung und Layer-Normalization
        Matrix attentionOutput = selfAttention.forward(input, input, input);
        Matrix outputAfterAttention = norm1.apply(input.add(attentionOutput));

        // 2. Feed-Forward-Netzwerk mit Residuenverbindung und Layer-Normalization
        Matrix feedForwardOutput = feedForward.forward(outputAfterAttention);
        Matrix finalOutput = norm2.apply(outputAfterAttention.add(feedForwardOutput));

        return finalOutput;
    }
}