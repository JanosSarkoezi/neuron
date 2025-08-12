package com.example.sandbox.transformer;

import com.example.sandbox.neuron.Matrix;

public class DecoderLayer {
    private final MultiHeadAttention selfAttention;
    private final MultiHeadAttention crossAttention;
    private final FeedForwardNetwork feedForward;
    private final LayerNormalization norm1;
    private final LayerNormalization norm2;
    private final LayerNormalization norm3;

    // Speichert Zwischenergebnisse für den Rückwärtspass
    private Matrix lastSelfAttentionInput;
    private Matrix lastCrossAttentionInput;
    private Matrix lastFFNInput;
    private Matrix lastSelfAttentionOutput;

    public DecoderLayer(int d_model, int numHeads, int d_ff) {
        this.selfAttention = new MultiHeadAttention(d_model, numHeads);
        this.crossAttention = new MultiHeadAttention(d_model, numHeads);
        this.feedForward = new FeedForwardNetwork(d_model, d_ff);
        this.norm1 = new LayerNormalization(d_model);
        this.norm2 = new LayerNormalization(d_model);
        this.norm3 = new LayerNormalization(d_model);
    }

    public Matrix forward(Matrix decoderInput, Matrix encoderOutput) {
        // Speichere die Eingabe für den Rückwärtspass der Self-Attention
        this.lastSelfAttentionInput = decoderInput;

        // 1. Maskierte Multi-Head Self-Attention
        // Der Maskierungsmechanismus (nicht implementiert) würde hier angewendet.
        Matrix maskedAttentionOutput = selfAttention.forward(decoderInput, decoderInput, decoderInput);
        this.lastSelfAttentionOutput = maskedAttentionOutput;
        Matrix outputAfterSelfAttention = norm1.forward(decoderInput.add(maskedAttentionOutput));

        // Speichere die Eingabe für den Rückwärtspass der Cross-Attention
        this.lastCrossAttentionInput = outputAfterSelfAttention;

        // 2. Multi-Head Cross-Attention
        // 'outputAfterSelfAttention' ist der Query, 'encoderOutput' ist Key und Value
        Matrix crossAttentionOutput = crossAttention.forward(outputAfterSelfAttention, encoderOutput, encoderOutput);
        Matrix outputAfterCrossAttention = norm2.forward(outputAfterSelfAttention.add(crossAttentionOutput));

        // Speichere die Eingabe für den Rückwärtspass des FFN
        this.lastFFNInput = outputAfterCrossAttention;

        // 3. Feed-Forward-Netzwerk
        Matrix feedForwardOutput = feedForward.forward(outputAfterCrossAttention);
        return norm3.forward(outputAfterCrossAttention.add(feedForwardOutput));
    }

    public Matrix backward(Matrix delta, Matrix encoderOutput) {
        // 1. Rückwärts durch die letzte Normalisierung und das FFN
        Matrix deltaAfterNorm3 = delta; // Simplification
        Matrix deltaFFN = feedForward.backward(deltaAfterNorm3);
        Matrix deltaBeforeFFN = deltaFFN; // Simplification

        // 2. Rückwärts durch die zweite Normalisierung und die Cross-Attention
        Matrix deltaAfterNorm2 = deltaBeforeFFN; // Simplification
        Matrix deltaCrossAttention = crossAttention.backward(deltaAfterNorm2, lastCrossAttentionInput, encoderOutput, encoderOutput);
        Matrix deltaBeforeCrossAttention = deltaCrossAttention; // Simplification

        // 3. Rückwärts durch die erste Normalisierung und die Self-Attention
        Matrix deltaAfterNorm1 = deltaBeforeCrossAttention; // Simplification
        Matrix deltaSelfAttention = selfAttention.backward(deltaAfterNorm1, lastSelfAttentionInput, lastSelfAttentionInput, lastSelfAttentionInput);

        return deltaSelfAttention;
    }
}