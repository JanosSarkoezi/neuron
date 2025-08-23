package com.example.sandbox.transformer;

import com.example.sandbox.neuron.Matrix;

public class DecoderLayer {
    private final MultiHeadAttention selfAttention;
    private final MultiHeadAttention crossAttention;
    private final FeedForwardNetwork feedForward;
    private final LayerNormalization norm1;
    private final LayerNormalization norm2;
    private final LayerNormalization norm3;

    public DecoderLayer(int d_model, int numHeads) {
        this.selfAttention = new MultiHeadAttention(d_model, numHeads);
        this.crossAttention = new MultiHeadAttention(d_model, numHeads);
        this.feedForward = new FeedForwardNetwork(d_model, d_model * 4);
        this.norm1 = new LayerNormalization(d_model);
        this.norm2 = new LayerNormalization(d_model);
        this.norm3 = new LayerNormalization(d_model);
    }

    public Matrix forward(Matrix input, Matrix encoderOutput) {
        // 1. Masked Self-Attention mit Residuenverbindung und Normalisierung
        // Hier müsste die Maskierung implementiert werden.
        Matrix maskedAttentionOutput = selfAttention.forward(input, input, input);
        Matrix outputAfterSelfAttention = norm1.apply(input.add(maskedAttentionOutput));

        // 2. Cross-Attention mit Residuenverbindung und Normalisierung
        // Query ist die Ausgabe der ersten Attention-Schicht
        // Key und Value sind die Ausgabe des Encoders
        Matrix crossAttentionOutput = crossAttention.forward(outputAfterSelfAttention, encoderOutput, encoderOutput);
        Matrix outputAfterCrossAttention = norm2.apply(outputAfterSelfAttention.add(crossAttentionOutput));

        // 3. Feed-Forward-Netzwerk mit Residuenverbindung und Normalisierung
        Matrix feedForwardOutput = feedForward.forward(outputAfterCrossAttention);
        Matrix finalOutput = norm3.apply(outputAfterCrossAttention.add(feedForwardOutput));

        return finalOutput;
    }
}