package com.example.sandbox.neuronal;

import java.util.function.DoubleUnaryOperator;

public record Activation(DoubleUnaryOperator activationFunction, DoubleUnaryOperator derivativeFunction) {

    public static Activation sigmoid() {
        DoubleUnaryOperator func = x -> 1.0 / (1.0 + Math.exp(-x));
        DoubleUnaryOperator deriv = x -> x * (1.0 - x);
        return new Activation(func, deriv);
    }

    public static Activation linear() {
        DoubleUnaryOperator func = x -> x;
        DoubleUnaryOperator deriv = x -> 1;
        return new Activation(func, deriv);
    }
}