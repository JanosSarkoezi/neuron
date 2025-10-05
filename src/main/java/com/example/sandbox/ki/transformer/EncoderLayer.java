package com.example.sandbox.ki.transformer;

import com.example.sandbox.ki.neuron.Matrix;

public class EncoderLayer {
    private final MultiHeadAttention selfAttention;
    private final FeedForwardNetwork feedForward;
    private final LayerNormalization norm1;
    private final LayerNormalization norm2;

    // Speichert die Zwischenergebnisse für den Rückwärtspass
    private Matrix lastAttentionInput;
    private Matrix lastFFNInput;

    public EncoderLayer(int d_model, int numHeads, int d_ff) {
        this.selfAttention = new MultiHeadAttention(d_model, numHeads);
        this.feedForward = new FeedForwardNetwork(d_model, d_ff);
        this.norm1 = new LayerNormalization(d_model);
        this.norm2 = new LayerNormalization(d_model);
    }

    public Matrix forward(Matrix input) {
        // Speichere die Eingabe für den Rückwärtspass
        this.lastAttentionInput = input;

        // 1. Multi-Head Self-Attention mit Residuenverbindung und Normalisierung
        Matrix attentionOutput = selfAttention.forward(input, input, input);
        Matrix outputAfterAttention = norm1.forward(input.add(attentionOutput));

        // Speichere die Eingabe für den Rückwärtspass des FFN
        this.lastFFNInput = outputAfterAttention;

        // 2. Feed-Forward-Netzwerk mit Residuenverbindung und Normalisierung
        Matrix feedForwardOutput = feedForward.forward(outputAfterAttention);
        return norm2.forward(outputAfterAttention.add(feedForwardOutput));
    }

    public Matrix backward(Matrix delta) {
        // Rückwärts durch die zweite Normalisierung und die Residuenverbindung
        Matrix deltaAfterNorm2 = delta; // Simplification

        // Rückwärts durch das Feed-Forward-Netzwerk
        Matrix deltaFFN = feedForward.backward(deltaAfterNorm2, lastFFNInput);

        // Rückwärts durch die erste Normalisierung und die Residuenverbindung
        Matrix deltaAfterNorm1 = deltaFFN; // Simplification

        // Rückwärts durch die Multi-Head Attention
        Matrix deltaFinal = selfAttention.backward(deltaAfterNorm1, lastAttentionInput, lastAttentionInput, lastAttentionInput);

        return deltaFinal;
    }
}