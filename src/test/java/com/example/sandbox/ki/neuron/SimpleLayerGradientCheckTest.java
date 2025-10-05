package com.example.sandbox.ki.neuron;

import com.example.sandbox.ki.neuron.activation.Sigmoid;
import com.example.sandbox.ki.neuron.loss.LossFunction;
import com.example.sandbox.ki.neuron.loss.MeanSquaredError;
import com.example.sandbox.ki.neuron.optimizer.Optimizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleLayerGradientCheckTest {

    private static final double EPSILON = 1e-6;
    private TestLayer testLayer;
    private Matrix aPrev;
    private Matrix expected;
    private LossFunction lossFunction;

    /**
     * A Layer subclass that spies on the gradients calculated in the backward pass.
     */
    private static class TestLayer extends Layer {
        public Matrix captured_dW;
        public Matrix captured_dB;

        public TestLayer(int inputSize, int outputSize) {
            super(inputSize, outputSize, new Sigmoid(), null, Matrix.random(outputSize, inputSize, 123), Matrix.random(outputSize, 1, 456));
        }

        // Override to capture gradients instead of updating weights
        @Override
        public void updateParameters(Matrix dW, Matrix dB, double learningRate) {
            this.captured_dW = dW.copy();
            this.captured_dB = dB.copy();
            // DO NOT call the optimizer
        }
    }

    @BeforeEach
    void setUp() {
        testLayer = new TestLayer(1, 1);
        // Use fixed values for reproducibility
        testLayer.setWeights(new Matrix(1, 1, new double[][]{{0.8}}));
        testLayer.setBiases(new Matrix(1, 1, new double[][]{{0.2}}));

        aPrev = new Matrix(1, 1, new double[][]{{0.5}}); // Activation from previous layer
        expected = new Matrix(1, 1, new double[][]{{1.0}}); // Expected output
        lossFunction = new MeanSquaredError();
    }

    @Test
    void checkWeightGradient() {
        // 1. Get analytical gradient from the backward pass
        Matrix a = testLayer.feedForward(aPrev.copy());
        Matrix delta = lossFunction.derivative(expected, a);
        testLayer.backward(delta, aPrev.copy());
        double analytical_dW = testLayer.captured_dW.get(0, 0);

        // 2. Calculate numerical gradient
        double numerical_dW = calculateNumericalGradientForMatrix(testLayer.getWeights(), 0, 0);

        // 3. Compare
        assertGradientCorrect("Weight (dW)", analytical_dW, numerical_dW);
    }

    @Test
    void checkBiasGradient() {
        // 1. Get analytical gradient from the backward pass
        Matrix a = testLayer.feedForward(aPrev.copy());
        Matrix delta = lossFunction.derivative(expected, a);
        testLayer.backward(delta, aPrev.copy());
        double analytical_dB = testLayer.captured_dB.get(0, 0);

        // 2. Calculate numerical gradient
        double numerical_dB = calculateNumericalGradientForMatrix(testLayer.getBiases(), 0, 0);

        // 3. Compare
        assertGradientCorrect("Bias (dB)", analytical_dB, numerical_dB);
    }

    @Test
    void checkActivationGradient() {
        // 1. Get analytical gradient from the backward pass
        Matrix a = testLayer.feedForward(aPrev.copy());
        Matrix delta = lossFunction.derivative(expected, a);
        Matrix analytical_dAPrev_matrix = testLayer.backward(delta, aPrev.copy());
        double analytical_dAPrev = analytical_dAPrev_matrix.get(0, 0);

        // 2. Calculate numerical gradient
        double numerical_dAPrev = calculateNumericalGradientForInput(0, 0);

        // 3. Compare
        assertGradientCorrect("Activation (dAPrev)", analytical_dAPrev, numerical_dAPrev);
    }

    private double calculateNumericalGradientForMatrix(Matrix targetMatrix, int row, int col) {
        double originalValue = targetMatrix.get(row, col);
        
        targetMatrix.set(row, col, originalValue + EPSILON);
        double lossPlus = lossFunction.loss(expected, testLayer.feedForward(aPrev.copy()));

        targetMatrix.set(row, col, originalValue - EPSILON);
        double lossMinus = lossFunction.loss(expected, testLayer.feedForward(aPrev.copy()));

        targetMatrix.set(row, col, originalValue); // Restore

        return (lossPlus - lossMinus) / (2 * EPSILON);
    }

    private double calculateNumericalGradientForInput(int row, int col) {
        double originalValue = aPrev.get(row, col);

        Matrix aPrevPlus = aPrev.copy();
        aPrevPlus.set(row, col, originalValue + EPSILON);
        double lossPlus = lossFunction.loss(expected, testLayer.feedForward(aPrevPlus));

        Matrix aPrevMinus = aPrev.copy();
        aPrevMinus.set(row, col, originalValue - EPSILON);
        double lossMinus = lossFunction.loss(expected, testLayer.feedForward(aPrevMinus));

        return (lossPlus - lossMinus) / (2 * EPSILON);
    }

    private void assertGradientCorrect(String name, double analytical, double numerical) {
        double relativeError = Math.abs(analytical - numerical) / (Math.abs(analytical) + Math.abs(numerical) + 1e-9);
        System.out.println("--- Checking " + name + " ---");
        System.out.println("Analytical: " + analytical);
        System.out.println("Numerical:  " + numerical);
        System.out.println("Relative Error: " + relativeError);
        assertTrue(relativeError < 1e-5, name + " gradient check failed! Relative error is too high.");
        System.out.println("OK!\n");
    }
}