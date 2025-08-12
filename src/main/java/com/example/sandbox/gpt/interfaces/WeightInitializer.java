package com.example.sandbox.gpt.interfaces;

import com.example.sandbox.gpt.Matrix;

import java.util.Random;

@FunctionalInterface
public interface WeightInitializer {
    Matrix initialize(int rows, int cols, Random random);
}