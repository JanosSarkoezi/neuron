package com.example.sandbox.neuron;

import java.util.function.DoubleUnaryOperator;
import java.util.Random;

public record Matrix(int rows, int cols, double[][] data) {

    public Matrix {
        if (data.length != rows || data[0].length != cols) {
            throw new IllegalArgumentException("Dimension mismatch in Matrix constructor");
        }
    }

    // Konstruktor für Spaltenvektor
    public Matrix(double[] vector) {
        this(vector.length, 1, toColumnVector(vector));
    }

    private static double[][] toColumnVector(double[] vector) {
        double[][] col = new double[vector.length][1];
        for (int i = 0; i < vector.length; i++) {
            col[i][0] = vector[i];
        }
        return col;
    }

    public static Matrix zeros(int rows, int cols) {
        return new Matrix(rows, cols, new double[rows][cols]);
    }

    public static Matrix random(int rows, int cols) {
        return random(rows, cols, new Random());
    }

    public static Matrix xavier(int rows, int cols, Random rand) {
        double scale = Math.sqrt(2.0 / (rows + cols));
        double[][] values = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                values[i][j] = rand.nextGaussian() * scale;
            }
        }
        return new Matrix(rows, cols, values);
    }

    public static Matrix random(int rows, int cols, Random rand) {
        double[][] values = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                values[i][j] = rand.nextGaussian();
            }
        }
        return new Matrix(rows, cols, values);
    }

    public static Matrix ones(int rows, int cols) {
        double[][] values = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                values[i][j] = 1.0;
            }
        }
        return new Matrix(rows, cols, values);
    }

    public static Matrix random(int rows, int cols, long seed) {
        return random(rows, cols, new Random(seed));
    }

    // --- Immutable Operations (return new Matrix) ---

    public Matrix add(Matrix other) {
        return this.copy().addInPlace(other);
    }

    public Matrix subtract(Matrix other) {
        return this.copy().subtractInPlace(other);
    }

    public Matrix multiply(double scalar) {
        return this.copy().multiplyInPlace(scalar);
    }

    public Matrix hadamard(Matrix other) {
        return this.copy().hadamardInPlace(other);
    }

    public Matrix add(double scalar) {
        return this.map(x -> x + scalar);
    }

    // ✅ MUTABLE: Addiert Skalar zu jedem Element (ändert diese Matrix)
    public Matrix addInPlace(double scalar) {
        return this.mapInPlace(x -> x + scalar);
    }

    // Square root für alle Elemente
    public Matrix sqrt() {
        return this.map(Math::sqrt); // Neue Matrix!
    }

    public Matrix reciprocal() {
        return this.map(x -> 1.0 / x); // Neue Matrix!
    }

    public Matrix square() {
        return this.hadamard(this); // Neue Matrix!
    }

    // ✅ KORREKT: Diese Methoden verändern das Objekt (mutable)
    public Matrix sqrtInPlace() {
        return this.mapInPlace(Math::sqrt); // Verändert this!
    }

    public Matrix reciprocalInPlace() {
        return this.mapInPlace(x -> 1.0 / x); // Verändert this!
    }

    public Matrix squareInPlace() {
        return this.hadamardInPlace(this); // Verändert this!
    }

    // Summe aller Elemente
    public double sum() {
        double total = 0.0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                total += data[i][j];
            }
        }
        return total;
    }

    // Mittelwert aller Elemente
    public double mean() {
        return sum() / (rows * cols);
    }

    /**
     * Berechnet den Mittelwert jedes Elements pro Zeile.
     * @return Eine Matrix (Spaltenvektor) mit den Mittelwerten.
     */
    public Matrix meanByRow() {
        double[][] means = new double[rows][1];
        for (int i = 0; i < rows; i++) {
            double sum = 0.0;
            for (int j = 0; j < cols; j++) {
                sum += data[i][j];
            }
            means[i][0] = sum / cols;
        }
        return new Matrix(rows, 1, means);
    }

    /**
     * Berechnet die Summe jedes Elements pro Zeile.
     * @return Eine Matrix (Spaltenvektor) mit den Summen.
     */
    public Matrix sumByRow() {
        double[][] sums = new double[rows][1];
        for (int i = 0; i < rows; i++) {
            double sum = 0.0;
            for (int j = 0; j < cols; j++) {
                sum += data[i][j];
            }
            sums[i][0] = sum;
        }
        return new Matrix(rows, 1, sums);
    }

    /**
     * Dividiert jedes Element der Matrix durch einen Skalar.
     * @param scalar Der Skalar, durch den geteilt wird.
     * @return Eine neue Matrix mit den Ergebnissen.
     */
    public Matrix divide(double scalar) {
        if (scalar == 0) {
            throw new IllegalArgumentException("Division by zero is not allowed.");
        }
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = data[i][j] / scalar;
            }
        }
        return new Matrix(rows, cols, result);
    }

    /**
     * Berechnet die Potenz jedes Elements der Matrix.
     * @param exponent Der Exponent.
     * @return Eine neue Matrix mit den potenziertem Elementen.
     */
    public Matrix pow(double exponent) {
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = Math.pow(data[i][j], exponent);
            }
        }
        return new Matrix(rows, cols, result);
    }


    public Matrix dot(Matrix other) {
        if (this.cols != other.rows) {
            throw new IllegalArgumentException("Dot product dimension mismatch: " +
                    this.rows + "x" + this.cols + " vs " + other.rows + "x" + other.cols);
        }

        double[][] result = new double[this.rows][other.cols];
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < other.cols; j++) {
                double sum = 0.0;
                for (int k = 0; k < this.cols; k++) {
                    sum += this.data[i][k] * other.data[k][j];
                }
                result[i][j] = sum;
            }
        }
        return new Matrix(this.rows, other.cols, result);
    }

    public Matrix transpose() {
        double[][] result = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[j][i] = this.data[i][j];
            }
        }
        return new Matrix(cols, rows, result);
    }

    public Matrix map(DoubleUnaryOperator f) {
        return this.copy().mapInPlace(f);
    }

    // --- Mutable Operations (modify this matrix) ---

    public Matrix addInPlace(Matrix other) {
        checkNotNull(other, "addition");
        checkDimensions(other, "addition");

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.data[i][j] += other.data[i][j];
            }
        }
        return this;
    }

    public Matrix subtractInPlace(Matrix other) {
        checkNotNull(other, "subtraction");
        checkDimensions(other, "subtraction");

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.data[i][j] -= other.data[i][j];
            }
        }
        return this;
    }

    public Matrix multiplyInPlace(double scalar) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.data[i][j] *= scalar;
            }
        }
        return this;
    }

    public Matrix hadamardInPlace(Matrix other) {
        checkNotNull(other, "Hadamard product");
        checkDimensions(other, "Hadamard product");

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.data[i][j] *= other.data[i][j];
            }
        }
        return this;
    }

    public Matrix mapInPlace(DoubleUnaryOperator f) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.data[i][j] = f.applyAsDouble(this.data[i][j]);
            }
        }
        return this;
    }

    // --- Utility Methods ---

    public Matrix copy() {
        double[][] newData = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(data[i], 0, newData[i], 0, cols);
        }
        return new Matrix(rows, cols, newData);
    }

    private void checkDimensions(Matrix other, String operation) {
        if (this.rows != other.rows || this.cols != other.cols) {
            throw new IllegalArgumentException("Matrix dimensions must match for " + operation +
                    ": " + this.rows + "x" + this.cols + " vs " + other.rows + "x" + other.cols);
        }
    }

    private void checkNotNull(Matrix other, String operation) {
        if (other == null) {
            throw new IllegalArgumentException("Matrix for " + operation + " must not be null");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Matrix(").append(rows).append("x").append(cols).append(")\n");
        for (int i = 0; i < Math.min(rows, 10); i++) { // Begrenze die Ausgabe auf die ersten 10 Zeilen
            sb.append("[");
            for (int j = 0; j < Math.min(cols, 10); j++) { // Begrenze die Ausgabe auf die ersten 10 Spalten
                sb.append(String.format("%8.4f", data[i][j]));
                if (j < cols - 1 && j < 9) sb.append(", "); // Komma nur wenn nicht letzte Spalte und innerhalb der ersten 10
            }
            if (cols > 10) {
                sb.append(", ..."); // Zeige an, wenn es mehr Spalten gibt
            }
            sb.append("]\n");
        }
        if (rows > 10) {
            sb.append("...\n"); // Zeige an, wenn es mehr Zeilen gibt
        }
        return sb.toString();
    }

    // Convenience method for getting a value
    public double get(int row, int col) {
        return data[row][col];
    }

    // Convenience method for setting a value (mutable)
    public void set(int row, int col, double value) {
        data[row][col] = value;
    }
}
