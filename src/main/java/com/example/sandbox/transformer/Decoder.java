package com.example.sandbox.transformer;

import com.example.sandbox.neuron.Matrix;
import java.util.ArrayList;
import java.util.List;

public class Decoder {
    private final List<DecoderLayer> layers;

    /**
     * Konstruktor für den Decoder.
     *
     * @param d_model   Dimension des Modells.
     * @param numHeads  Anzahl der Aufmerksamkeitsköpfe.
     * @param d_ff      Dimension des Feed-Forward-Netzwerks.
     * @param numLayers Anzahl der Decoder-Layer.
     */
    public Decoder(int d_model, int numHeads, int d_ff, int numLayers) {
        this.layers = new ArrayList<>();
        for (int i = 0; i < numLayers; i++) {
            // Der DecoderLayer-Konstruktor benötigt ebenfalls drei Parameter
            this.layers.add(new DecoderLayer(d_model, numHeads, d_ff));
        }
    }

    /**
     * Führt den Vorwärtspass durch alle Decoder-Layer aus.
     *
     * @param input         Die Eingabe des Decoders (Output-Sequenz).
     * @param encoderOutput Die Ausgabe des Encoders.
     * @return Die Ausgabe des letzten Decoder-Layers.
     */
    public Matrix forward(Matrix input, Matrix encoderOutput) {
        Matrix output = input;
        for (DecoderLayer layer : layers) {
            output = layer.forward(output, encoderOutput);
        }
        return output;
    }

    /**
     * Führt den Rückwärtspass durch alle Decoder-Layer in umgekehrter Reihenfolge aus.
     *
     * @param delta         Der Gradient, der vom Ausgabelayer kommt.
     * @param encoderOutput Die Ausgabe des Encoders, da sie für Cross-Attention benötigt wird.
     * @return Das Delta, das an die vorhergehende Schicht weitergegeben wird.
     */
    public Matrix backward(Matrix delta, Matrix encoderOutput) {
        Matrix currentDelta = delta;
        for (int i = layers.size() - 1; i >= 0; i--) {
            DecoderLayer layer = layers.get(i);
            currentDelta = layer.backward(currentDelta, encoderOutput);
        }
        return currentDelta;
    }
}