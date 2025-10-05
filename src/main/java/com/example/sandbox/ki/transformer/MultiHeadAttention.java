package com.example.sandbox.ki.transformer;

import com.example.sandbox.ki.neuron.Layer;
import com.example.sandbox.ki.neuron.Matrix;
import com.example.sandbox.ki.neuron.activation.Softmax;
import com.example.sandbox.ki.neuron.builder.LayerBuilder;
import com.example.sandbox.ki.neuron.optimizer.AdamOptimizer;

public class MultiHeadAttention {
    private final int d_model;
    private final int numHeads;
    private final int d_k;
    private final Softmax softmax;

    // Refactored to use Layer objects for linear projections
    private final Layer q_layer, k_layer, v_layer, o_layer;

    // Intermediate results stored for the backward pass
    private Matrix Q_proj, K_proj, V_proj;
    private Matrix attentionScores;
    private Matrix attentionWeights;
    private Matrix attentionOutput;
    private Matrix lastQuery, lastKey, lastValue; // Need to store original inputs for backward pass

    public MultiHeadAttention(int d_model, int numHeads) {
        this.d_model = d_model;
        this.numHeads = numHeads;
        this.d_k = d_model / numHeads;
        this.softmax = new Softmax();

        // Initialize Layer objects for Q, K, V, and O projections
        LayerBuilder builder = new LayerBuilder().withSize(d_model, d_model).withActivation(() -> null).withOptimizerBySize(AdamOptimizer::new);
        this.q_layer = builder.build();
        this.k_layer = builder.build();
        this.v_layer = builder.build();
        this.o_layer = builder.build();
    }

    public Matrix forward(Matrix query, Matrix key, Matrix value) {
        return this.forward(query, key, value, null);
    }

    public Matrix forward(Matrix query, Matrix key, Matrix value, Matrix mask) {
        // Store original inputs for backward pass
        this.lastQuery = query;
        this.lastKey = key;
        this.lastValue = value;

        // 1. Linear Projections using Layers
        this.Q_proj = q_layer.feedForward(query);
        this.K_proj = k_layer.feedForward(key);
        this.V_proj = v_layer.feedForward(value);

        // 2. Scaled Dot-Product Attention
        this.attentionScores = Q_proj.dot(K_proj.transpose()).multiply(1.0 / Math.sqrt(d_k));
        if (mask != null) {
            this.attentionScores = this.attentionScores.add(mask);
        }
        this.attentionWeights = softmax.apply(attentionScores);
        this.attentionOutput = attentionWeights.dot(V_proj);

        // 3. Final Linear Projection using a Layer
        return o_layer.feedForward(attentionOutput);
    }

    public Matrix backward(Matrix delta, Matrix query, Matrix key, Matrix value) {
        // 1. Backprop through the final output layer (o_layer)
        Matrix dAttentionOutput = o_layer.backward(delta, this.attentionOutput);

        // 2. Backprop through the attention mechanism (same logic as before)
        Matrix dV_proj = attentionWeights.transpose().dot(dAttentionOutput);
        Matrix dAttentionWeights = dAttentionOutput.dot(V_proj.transpose());
        Matrix dAttentionScores = softmax.derivative(attentionScores).hadamard(dAttentionWeights);
        Matrix dQ_proj = dAttentionScores.dot(K_proj).multiply(1.0 / Math.sqrt(d_k));
        Matrix dK_proj = dAttentionScores.transpose().dot(Q_proj).multiply(1.0 / Math.sqrt(d_k));

        // 3. Backprop through the Q, K, V projection layers
        Matrix dQ = q_layer.backward(dQ_proj, this.lastQuery);
        Matrix dK = k_layer.backward(dK_proj, this.lastKey);
        Matrix dV = v_layer.backward(dV_proj, this.lastValue);

        // The total delta for the previous layer is the sum of the deltas from Q, K, and V
        return dQ.add(dK).add(dV);
    }
}