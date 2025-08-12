package com.example.sandbox.gemini.neuronal.util;

public class MatrixOperations {
    /**
     * Führt eine Matrix-Vektor-Multiplikation durch.
     * @param matrix Die Matrix als zweidimensionales Array.
     * @param vector Der Vektor als eindimensionales Array.
     * @return Das Ergebnis der Multiplikation als neuer Vektor.
     */
    public static double[] multiply(double[][] matrix, double[] vector) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        if (cols != vector.length) {
            throw new IllegalArgumentException("Matrixspalten müssen mit Vektorzeilen übereinstimmen.");
        }

        double[] result = new double[rows];
        for (int i = 0; i < rows; i++) {
            double sum = 0.0;
            for (int j = 0; j < cols; j++) {
                sum += matrix[i][j] * vector[j];
            }
            result[i] = sum;
        }
        return result;
    }

    /**
     * Führt eine Matrix-Vektor-Multiplikation mit einer transponierten Matrix durch.
     * Dies entspricht A^T * v.
     * @param matrix Die Matrix.
     * @param vector Der Vektor.
     * @return Das Ergebnis der Multiplikation.
     */
    public static double[] multiplyWithTranspose(double[][] matrix, double[] vector) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        if (rows != vector.length) {
            throw new IllegalArgumentException("Vektorlänge muss mit der Zeilenzahl der Matrix übereinstimmen.");
        }

        double[] result = new double[cols];
        for (int i = 0; i < cols; i++) {
            double sum = 0.0;
            for (int j = 0; j < rows; j++) {
                sum += matrix[j][i] * vector[j];
            }
            result[i] = sum;
        }
        return result;
    }
}