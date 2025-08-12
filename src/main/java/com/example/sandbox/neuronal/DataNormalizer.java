package com.example.sandbox.neuronal;

public class DataNormalizer {
    // Hilfsmethode zum Normalisieren mit variablen Grenzen
    public static double normalize(int val, double min, double max) {
        return (val - min) / (max - min);
    }

    // Hilfsmethode zum Denormalisieren mit variablen Grenzen
    public static int denormalize(double val, double min, double max) {
        int num = (int) Math.round(val * (max - min) + min);
        // Sicherstellen, dass der Wert innerhalb der ursprünglichen Grenzen bleibt
        return Math.max((int) min, Math.min((int) max, num));
    }
}
