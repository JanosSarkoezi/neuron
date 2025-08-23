package com.example.sandbox.transformer;

import com.example.sandbox.neuron.Matrix;
import java.util.ArrayList;
import java.util.List;

public class Encoder {
    private final List<EncoderLayer> layers;

    // Annahme: d_model und numHeads werden an den Konstruktor übergeben
    public Encoder(int d_model, int numHeads, int numLayers) {
        this.layers = new ArrayList<>();
        for (int i = 0; i < numLayers; i++) {
            this.layers.add(new EncoderLayer(d_model, numHeads));
        }
    }

    public Matrix forward(Matrix input) {
        Matrix output = input;
        for (EncoderLayer layer : layers) {
            output = layer.forward(output);
        }
        return output;
    }
}