package com.example.sandbox.ki.neuron.optimizer;

import com.example.sandbox.ki.neuron.Layer;
import com.example.sandbox.ki.neuron.Matrix;

public class AdamOptimizer implements Optimizer {
    // Adam-spezifische Variablen
    private Matrix m_w;
    private Matrix v_w;
    private Matrix m_b;
    private Matrix v_b;
    private int t = 0;

    private final double epsilon = 1e-8;

    public AdamOptimizer(int inputSize, int outputSize) {
        this.m_w = Matrix.zeros(outputSize, inputSize);
        this.v_w = Matrix.zeros(outputSize, inputSize);
        this.m_b = Matrix.zeros(outputSize, 1);
        this.v_b = Matrix.zeros(outputSize, 1);
    }

    @Override
    public void update(Layer layer, Matrix dW, Matrix dB, double learningRate) {
        t++;

        double beta1 = 0.9;
        double beta2 = 0.999;
        // Update der Momente
        // Adam-Hyperparameter
        m_w = m_w.multiply(beta1).add(dW.multiply(1 - beta1));
        v_w = v_w.multiply(beta2).add(dW.hadamard(dW).multiply(1 - beta2));
        m_b = m_b.multiply(beta1).add(dB.multiply(1 - beta1));
        v_b = v_b.multiply(beta2).add(dB.hadamard(dB).multiply(1 - beta2));

        // Bias-Korrektur
        Matrix m_w_hat = m_w.multiply(1.0 / (1.0 - Math.pow(beta1, t)));
        Matrix v_w_hat = v_w.multiply(1.0 / (1.0 - Math.pow(beta2, t)));
        Matrix m_b_hat = m_b.multiply(1.0 / (1.0 - Math.pow(beta1, t)));
        Matrix v_b_hat = v_b.multiply(1.0 / (1.0 - Math.pow(beta2, t)));

        Matrix dw_update = m_w_hat.hadamard(v_w_hat.map(x -> Math.sqrt(x) + epsilon).map(x -> 1.0 / x));
        Matrix db_update = m_b_hat.hadamard(v_b_hat.map(x -> Math.sqrt(x) + epsilon).map(x -> 1.0 / x));

        layer.setWeights(layer.getWeights().subtract(dw_update.multiply(learningRate)));
        layer.setBiases(layer.getBiases().subtract(db_update.multiply(learningRate)));
    }
}