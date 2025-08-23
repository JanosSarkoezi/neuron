package com.example.sandbox.transformer;

import com.example.sandbox.neuron.Matrix;
import java.util.ArrayList;
import java.util.List;

public class Decoder {
    private final List<DecoderLayer> layers;

    // Annahme: d_model, numHeads und numLayers werden übergeben
    public Decoder(int d_model, int numHeads, int numLayers) {
        this.layers = new ArrayList<>();
        for (int i = 0; i < numLayers; i++) {
            this.layers.add(new DecoderLayer(d_model, numHeads));
        }
    }

    public Matrix forward(Matrix input, Matrix encoderOutput) {
        Matrix output = input;
        for (DecoderLayer layer : layers) {
            output = layer.forward(output, encoderOutput);
        }
        return output;
    }
}