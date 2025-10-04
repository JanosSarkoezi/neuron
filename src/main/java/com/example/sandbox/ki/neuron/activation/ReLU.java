package com.example.sandbox.ki.neuron.activation;

import com.example.sandbox.ki.neuron.Matrix;

public class ReLU implements ActivationFunction {
    @Override
    public Matrix apply(Matrix z) {
        return z.map(x -> Math.max(0, x));
    }

    @Override
    public Matrix derivative(Matrix z) {
        return z.map(x -> x > 0 ? 1.0 : 0.0);
    }
}
