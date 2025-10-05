package com.example.sandbox.ki.transformer;

import com.example.sandbox.ki.neuron.Matrix;
import java.util.ArrayList;
import java.util.List;

public class Encoder {
    private final List<EncoderLayer> layers;

    /**
     * Konstruktor für den Encoder.
     *
     * @param d_model   Dimension des Modells.
     * @param numHeads  Anzahl der Aufmerksamkeitsköpfe.
     * @param d_ff      Dimension des Feed-Forward-Netzwerks.
     * @param numLayers Anzahl der Encoder-Layer.
     */
    public Encoder(int d_model, int numHeads, int d_ff, int numLayers, double dropoutRate) {
        this.layers = new ArrayList<>();
        for (int i = 0; i < numLayers; i++) {
            // Hier wird der EncoderLayer-Konstruktor mit 3 Parametern aufgerufen
            this.layers.add(new EncoderLayer(d_model, numHeads, d_ff, dropoutRate));
        }
    }

    /**
     * Führt den Vorwärtspass durch alle Encoder-Layer aus.
     *
     * @param input Die Eingabe-Matrix.
     * @return Die Ausgabe des letzten Encoder-Layers.
     */
    public Matrix forward(Matrix input) {
        Matrix output = input;
        for (EncoderLayer layer : layers) {
            output = layer.forward(output);
        }
        return output;
    }

    /**
     * Führt den Rückwärtspass durch alle Encoder-Layer in umgekehrter Reihenfolge aus.
     *
     * @param delta Der Gradient, der vom Decoder kommt.
     * @return Das Delta, das an die vorhergehende Schicht weitergegeben wird.
     */
    public Matrix backward(Matrix delta) {
        Matrix currentDelta = delta;
        for (int i = layers.size() - 1; i >= 0; i--) {
            EncoderLayer layer = layers.get(i);
            currentDelta = layer.backward(currentDelta);
        }
        return currentDelta;
    }
}