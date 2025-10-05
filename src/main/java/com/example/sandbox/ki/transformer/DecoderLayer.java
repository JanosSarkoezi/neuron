package com.example.sandbox.ki.transformer;

import com.example.sandbox.ki.neuron.Matrix;

import java.util.Random;

public class DecoderLayer {
    private final MultiHeadAttention selfAttention;
    private final MultiHeadAttention crossAttention;
    private final FeedForwardNetwork feedForward;
    private final LayerNormalization norm1;
    private final LayerNormalization norm2;
    private final LayerNormalization norm3;
    private final double dropoutRate;
    private final Random random = new Random();

    // Speichert Zwischenergebnisse für den Rückwärtspass
    private Matrix lastSelfAttentionInput;
    private Matrix lastCrossAttentionInput;
    private Matrix lastFFNInput;
    private Matrix dropoutMask1, dropoutMask2, dropoutMask3;

    public DecoderLayer(int d_model, int numHeads, int d_ff, double dropoutRate) {
        this.selfAttention = new MultiHeadAttention(d_model, numHeads);
        this.crossAttention = new MultiHeadAttention(d_model, numHeads);
        this.feedForward = new FeedForwardNetwork(d_model, d_ff);
        this.norm1 = new LayerNormalization(d_model);
        this.norm2 = new LayerNormalization(d_model);
        this.norm3 = new LayerNormalization(d_model);
        this.dropoutRate = dropoutRate;
    }

    public Matrix forward(Matrix decoderInput, Matrix encoderOutput) {
        // Speichere die Eingabe für den Rückwärtspass der Self-Attention
        this.lastSelfAttentionInput = decoderInput;

        // 1. Maskierte Multi-Head Self-Attention
        Matrix maskedAttentionOutput = selfAttention.forward(decoderInput, decoderInput, decoderInput, createLookAheadMask(decoderInput.rows()));
        this.dropoutMask1 = createDropoutMask(maskedAttentionOutput);
        Matrix maskedAttentionOutputAfterDropout = maskedAttentionOutput.hadamard(this.dropoutMask1);
        Matrix outputAfterSelfAttention = norm1.forward(decoderInput.add(maskedAttentionOutputAfterDropout));

        // Speichere die Eingabe für den Rückwärtspass der Cross-Attention
        this.lastCrossAttentionInput = outputAfterSelfAttention;

        // 2. Multi-Head Cross-Attention
        Matrix crossAttentionOutput = crossAttention.forward(outputAfterSelfAttention, encoderOutput, encoderOutput);
        this.dropoutMask2 = createDropoutMask(crossAttentionOutput);
        Matrix crossAttentionOutputAfterDropout = crossAttentionOutput.hadamard(this.dropoutMask2);
        Matrix outputAfterCrossAttention = norm2.forward(outputAfterSelfAttention.add(crossAttentionOutputAfterDropout));

        // Speichere die Eingabe für den Rückwärtspass des FFN
        this.lastFFNInput = outputAfterCrossAttention;

        // 3. Feed-Forward-Netzwerk
        Matrix feedForwardOutput = feedForward.forward(outputAfterCrossAttention);
        this.dropoutMask3 = createDropoutMask(feedForwardOutput);
        Matrix feedForwardOutputAfterDropout = feedForwardOutput.hadamard(this.dropoutMask3);
        return norm3.forward(outputAfterCrossAttention.add(feedForwardOutputAfterDropout));
    }
    
    private Matrix createLookAheadMask(int size) {
        double[][] maskData = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (j > i) {
                    maskData[i][j] = Double.NEGATIVE_INFINITY;
                }
            }
        }
        return new Matrix(size, size, maskData);
    }

    private Matrix createDropoutMask(Matrix m) {
        double scale = 1.0 / (1.0 - this.dropoutRate);
        return Matrix.random(m.rows(), m.cols(), this.random)
                .mapInPlace(x -> (x < this.dropoutRate) ? 0.0 : scale);
    }

    public Matrix backward(Matrix delta, Matrix encoderOutput) {
        // 1. Rückwärts durch die letzte Normalisierung, Dropout und FFN (vereinfacht)
        Matrix deltaAfterNorm3 = delta; // Simplification
        Matrix deltaAfterDropout3 = deltaAfterNorm3.hadamard(this.dropoutMask3);
        Matrix deltaFFN = feedForward.backward(deltaAfterDropout3, lastFFNInput);

        // 2. Rückwärts durch die zweite Normalisierung, Dropout und Cross-Attention (vereinfacht)
        Matrix deltaAfterNorm2 = deltaFFN; // Simplification
        Matrix deltaAfterDropout2 = deltaAfterNorm2.hadamard(this.dropoutMask2);
        Matrix deltaCrossAttention = crossAttention.backward(deltaAfterDropout2, lastCrossAttentionInput, encoderOutput, encoderOutput);

        // 3. Rückwärts durch die erste Normalisierung, Dropout und Self-Attention (vereinfacht)
        Matrix deltaAfterNorm1 = deltaCrossAttention; // Simplification
        Matrix deltaAfterDropout1 = deltaAfterNorm1.hadamard(this.dropoutMask1);
        Matrix deltaSelfAttention = selfAttention.backward(deltaAfterDropout1, lastSelfAttentionInput, lastSelfAttentionInput, lastSelfAttentionInput);

        return deltaSelfAttention;
    }
}