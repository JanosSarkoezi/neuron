package com.example.sandbox.neuron.activation;

import com.example.sandbox.neuron.Matrix;

public class Sigmoid implements ActivationFunction {
    @Override
    public Matrix apply(Matrix z) {
        return z.map(x -> 1.0 / (1.0 + Math.exp(-x)));
    }

    @Override
    public Matrix derivative(Matrix z) {
        // f'(x) = f(x) * (1 - f(x))
        Matrix sigmoid = apply(z);
        return sigmoid.hadamard(sigmoid.map(x -> 1.0 - x));
    }
}
