package com.example.sandbox.markov;

import com.example.sandbox.neuron.Matrix;

import java.util.ArrayList;
import java.util.List;

public class MarkovMatrix {

    private final int size;
    private final Matrix counts;

    /**
     * Konstruktor für die MarkovMatrix.
     *
     * @param size Die maximale Zahl, die im Lotto gezogen werden kann (z.B. 49).
     */
    public MarkovMatrix(int size) {
        this.size = size;
        // Die counts-Matrix ist nun size x size groß, für Zahlen von 1 bis size.
        // Wir verwenden eine Hilfsmethode, um die Index-Umrechnung zu verwalten.
        this.counts = Matrix.zeros(size, size);
    }

    /**
     * Fügt eine Lottoziehung zur Berechnung der Übergangshäufigkeiten hinzu.
     *
     * @param zahlen Eine Liste der gezogenen Zahlen in einer Ziehung.
     */
    public void addZiehung(List<Integer> zahlen) {
        if (zahlen.size() < 2) {
            return;
        }

        // Erstelle eine veränderbare Kopie der Liste
        List<Integer> mutableZahlen = new ArrayList<>(zahlen);

        // Sortiere die veränderbare Kopie
        mutableZahlen.sort(Integer::compareTo);

        for (int i = 0; i < mutableZahlen.size() - 1; i++) {
            int from = mutableZahlen.get(i) - 1;
            int to = mutableZahlen.get(i + 1) - 1;

            if (from >= 1 && from <= size && to >= 1 && to <= size) {
                double currentValue = counts.get(from, to);
                counts.set(from, to, currentValue + 1);
            }
        }
    }

    /**
     * Erstellt die normalisierte Wahrscheinlichkeitsmatrix.
     *
     * @return Eine Matrix, in der die Werte die Übergangswahrscheinlichkeiten darstellen.
     */
    public Matrix buildProbabilityMatrix() {
        Matrix probabilityMatrix = Matrix.zeros(size, size);

        // Iteriere über die rows und berechne die Zeilensummen
        for (int i = 0; i < size; i++) {
            double sum = 0.0;
            for (int j = 0; j < size; j++) {
                sum += counts.get(i, j);
            }

            if (sum > 0) {
                for (int j = 0; j < size; j++) {
                    double probability = counts.get(i, j) / sum;
                    probabilityMatrix.set(i, j, probability);
                }
            }
        }
        return probabilityMatrix;
    }
}