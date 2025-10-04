package com.example.sandbox.ki.neuron.interfaces;

import com.example.sandbox.ki.neuron.Matrix;

import java.util.Random;

@FunctionalInterface
public interface WeightInitializer {
    Matrix initialize(int rows, int cols, Random random);
}