package com.example.sandbox.neuron;

import com.example.sandbox.ki.neuron.Matrix;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MatrixTest {

    @Test
    public void testAdd() {
        double[][] aData = {{1, 2}, {3, 4}};
        double[][] bData = {{5, 6}, {7, 8}};
        Matrix a = new Matrix(2, 2, aData);
        Matrix b = new Matrix(2, 2, bData);

        Matrix result = a.add(b);

        assertEquals(6.0, result.data()[0][0], 1e-9);
        assertEquals(8.0, result.data()[0][1], 1e-9);
        assertEquals(10.0, result.data()[1][0], 1e-9);
        assertEquals(12.0, result.data()[1][1], 1e-9);
    }

    @Test
    public void testSubtract() {
        Matrix a = new Matrix(2, 2, new double[][]{{3, 5}, {7, 9}});
        Matrix b = new Matrix(2, 2, new double[][]{{1, 2}, {3, 4}});

        Matrix result = a.subtract(b);

        assertEquals(2.0, result.data()[0][0], 1e-9);
        assertEquals(3.0, result.data()[0][1], 1e-9);
        assertEquals(4.0, result.data()[1][0], 1e-9);
        assertEquals(5.0, result.data()[1][1], 1e-9);
    }

    @Test
    public void testDotProduct() {
        Matrix a = new Matrix(2, 3, new double[][]{
                {1, 2, 3},
                {4, 5, 6}
        });
        Matrix b = new Matrix(3, 2, new double[][]{
                {7, 8},
                {9, 10},
                {11, 12}
        });

        Matrix result = a.dot(b);

        assertEquals(58.0, result.data()[0][0], 1e-9); // 1*7+2*9+3*11
        assertEquals(64.0, result.data()[0][1], 1e-9); // 1*8+2*10+3*12
        assertEquals(139.0, result.data()[1][0], 1e-9);
        assertEquals(154.0, result.data()[1][1], 1e-9);
    }

    @Test
    public void testTranspose() {
        Matrix a = new Matrix(2, 3, new double[][]{
                {1, 2, 3},
                {4, 5, 6}
        });

        Matrix result = a.transpose();

        assertEquals(1.0, result.data()[0][0], 1e-9);
        assertEquals(4.0, result.data()[0][1], 1e-9);
        assertEquals(2.0, result.data()[1][0], 1e-9);
        assertEquals(5.0, result.data()[1][1], 1e-9);
        assertEquals(3.0, result.data()[2][0], 1e-9);
        assertEquals(6.0, result.data()[2][1], 1e-9);
    }

    @Test
    public void testHadamard() {
        Matrix a = new Matrix(2, 2, new double[][]{{1, 2}, {3, 4}});
        Matrix b = new Matrix(2, 2, new double[][]{{5, 6}, {7, 8}});

        Matrix result = a.hadamard(b);

        assertEquals(5.0, result.data()[0][0], 1e-9);
        assertEquals(12.0, result.data()[0][1], 1e-9);
        assertEquals(21.0, result.data()[1][0], 1e-9);
        assertEquals(32.0, result.data()[1][1], 1e-9);
    }

    @Test
    public void testMultiplyScalar() {
        Matrix a = new Matrix(2, 2, new double[][]{{1, -2}, {-3, 4}});

        Matrix result = a.multiply(2.0);

        assertEquals(2.0, result.data()[0][0], 1e-9);
        assertEquals(-4.0, result.data()[0][1], 1e-9);
        assertEquals(-6.0, result.data()[1][0], 1e-9);
        assertEquals(8.0, result.data()[1][1], 1e-9);
    }
}
