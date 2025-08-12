package com.example.sandbox.neuron;

import com.example.sandbox.neuron.activation.ActivationFunction;
import com.example.sandbox.neuron.loss.LossFunction;

import java.util.*;
import java.util.function.Supplier;

public class NeuralNetwork {
    private final List<Layer> layers;
    private final LossFunction lossFunction;

    // Dependency Injection über den Konstruktor: Der Optimizer wird hier übergeben.
    public NeuralNetwork(Supplier<LossFunction> lossFunction) {
        this.layers = new ArrayList<>();
        this.lossFunction = lossFunction.get();
    }

    public void addLayer(Layer layer) {
        this.layers.add(layer);
    }

    public Matrix predict(Matrix input) {
        Matrix current = input;
        for (Layer layer : layers) {
            current = layer.feedForward(current);
        }
        return current;
    }

    public LossFunction getLossFunction() {
        return lossFunction;
    }

    public void train(Matrix input, Matrix expected, double learningRate) {
        // Forward-Pass
        Matrix output = predict(input);

        // Initiales Delta (Loss-Gradient wrt Output)
        Matrix delta = lossFunction.derivative(expected, output);

        // Backward-Pass durch alle Layer
        for (int l = layers.size() - 1; l >= 0; l--) {
            Layer layer = layers.get(l);

            ActivationFunction activation = layer.getActivationFunction();
            delta = delta.hadamard(activation.derivative(layer.getLastZ()));

            Matrix aPrev = (l == 0) ? input : layers.get(l - 1).getLastA();
            Matrix dW = delta.dot(aPrev.transpose());
            Matrix dB = delta;

            layer.updateParameters(dW, dB, learningRate);

            // Delta für vorherige Schicht
            if (l > 0) {
                delta = layer.getWeights().transpose().dot(delta);
            }
        }
    }

    public void train2(Matrix input, Matrix expected, double learningRate) {
        // Forward-Pass (Aktivierungen werden in Layern gespeichert)
        Matrix output = predict(input);

        // Backward-Pass mit gespeicherten Deltas
        List<Matrix> deltas = new ArrayList<>();

        // 1. Output Layer Delta berechnen
        Matrix delta = lossFunction.derivative(expected, output);
        Matrix outputActivationDeriv = layers.get(layers.size() - 1)
                .getActivationFunction()
                .derivative(layers.get(layers.size() - 1).getLastZ());
        delta = delta.hadamard(outputActivationDeriv);
        deltas.addFirst(delta); // Am Anfang einfügen

        // 2. Rückwärts durch versteckte Layer
        for (int l = layers.size() - 2; l >= 0; l--) { // Ab vorletztem Layer
            Layer currentLayer = layers.get(l);
            Layer nextLayer = layers.get(l + 1);

            // Delta durch Gewichte der NÄCHSTEN Schicht propagieren
            delta = nextLayer.getWeights().transpose().dot(deltas.get(0));

            // Mit Aktivierungsableitung der AKTUELLEN Schicht multiplizieren
            Matrix activationDeriv = currentLayer.getActivationFunction()
                    .derivative(currentLayer.getLastZ());
            delta = delta.hadamard(activationDeriv);

            deltas.addFirst(delta); // Am Anfang einfügen
        }

        // 3. Jetzt alle Parameter updaten
        for (int l = 0; l < layers.size(); l++) {
            Layer layer = layers.get(l);
            Matrix deltaL = deltas.get(l);

            // Richtige Eingabe-Aktivierung finden
            Matrix aPrev = (l == 0) ? input : layers.get(l - 1).getLastA();

            // Gradienten berechnen
            Matrix dW = deltaL.dot(aPrev.transpose());
            Matrix dB = deltaL; // Für Bias (kann auch meanColumnwise() sein)

            // Parameter updaten
            layer.updateParameters(dW, dB, learningRate);
        }
    }
}
