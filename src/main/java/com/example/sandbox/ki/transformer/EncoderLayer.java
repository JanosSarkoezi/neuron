package com.example.sandbox.ki.transformer;

import com.example.sandbox.ki.neuron.Matrix;

import java.util.Random;

public class EncoderLayer {
    private final MultiHeadAttention selfAttention;
    private final FeedForwardNetwork feedForward;
    private final LayerNormalization norm1;
    private final LayerNormalization norm2;
    private final double dropoutRate;
    private final Random random = new Random();

    // Speichert die Zwischenergebnisse für den Rückwärtspass
    private Matrix lastAttentionInput;
    private Matrix lastFFNInput;
    private Matrix dropoutMask1;
    private Matrix dropoutMask2;

    public EncoderLayer(int d_model, int numHeads, int d_ff, double dropoutRate) {
        this.selfAttention = new MultiHeadAttention(d_model, numHeads);
        this.feedForward = new FeedForwardNetwork(d_model, d_ff);
        this.norm1 = new LayerNormalization(d_model);
        this.norm2 = new LayerNormalization(d_model);
        this.dropoutRate = dropoutRate;
    }

    public Matrix forward(Matrix input) {
        // Speichere die Eingabe für den Rückwärtspass
        this.lastAttentionInput = input;

        // 1. Multi-Head Self-Attention
        Matrix attentionOutput = selfAttention.forward(input, input, input);
        // Dropout
        this.dropoutMask1 = createDropoutMask(attentionOutput);
        Matrix attentionOutputAfterDropout = attentionOutput.hadamard(this.dropoutMask1);
        // Residuenverbindung und Normalisierung
        Matrix outputAfterAttention = norm1.forward(input.add(attentionOutputAfterDropout));

        // Speichere die Eingabe für den Rückwärtspass des FFN
        this.lastFFNInput = outputAfterAttention;

        // 2. Feed-Forward-Netzwerk
        Matrix feedForwardOutput = feedForward.forward(outputAfterAttention);
        // Dropout
        this.dropoutMask2 = createDropoutMask(feedForwardOutput);
        Matrix feedForwardOutputAfterDropout = feedForwardOutput.hadamard(this.dropoutMask2);
        // Residuenverbindung und Normalisierung
        return norm2.forward(outputAfterAttention.add(feedForwardOutputAfterDropout));
    }

    private Matrix createDropoutMask(Matrix m) {
        double scale = 1.0 / (1.0 - this.dropoutRate);
        return Matrix.random(m.rows(), m.cols(), this.random)
                .mapInPlace(x -> (x < this.dropoutRate) ? 0.0 : scale);
    }

    public Matrix backward(Matrix delta) {
        // Rückwärts durch die zweite Normalisierung und die Residuenverbindung (vereinfacht)
        Matrix deltaAfterNorm2 = delta; // Simplification
        // Rückwärts durch Dropout 2
        Matrix deltaAfterDropout2 = deltaAfterNorm2.hadamard(this.dropoutMask2);

        // Rückwärts durch das Feed-Forward-Netzwerk
        Matrix deltaFFN = feedForward.backward(deltaAfterDropout2, lastFFNInput);

        // Rückwärts durch die erste Normalisierung und die Residuenverbindung (vereinfacht)
        Matrix deltaAfterNorm1 = deltaFFN; // Simplification
        // Rückwärts durch Dropout 1
        Matrix deltaAfterDropout1 = deltaAfterNorm1.hadamard(this.dropoutMask1);

        // Rückwärts durch die Multi-Head Attention
        Matrix deltaFinal = selfAttention.backward(deltaAfterDropout1, lastAttentionInput, lastAttentionInput, lastAttentionInput);

        return deltaFinal;
    }
}