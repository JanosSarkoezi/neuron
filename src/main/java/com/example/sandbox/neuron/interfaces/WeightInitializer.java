package com.example.sandbox.neuron.interfaces;

import com.example.sandbox.neuron.Matrix;

import java.util.Random;

@FunctionalInterface
public interface WeightInitializer {
    Matrix initialize(int rows, int cols, Random random);
}