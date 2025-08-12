package com.example.sandbox.gemini.neuronal;

import com.example.sandbox.gemini.neuronal.activation.ActivationFunction;

import java.util.ArrayList;
import java.util.List;

public class Layer {
    private final List<Neuron> neurons;
    private final ActivationFunction activationFunction;
    private double[] lastOutputSums;
    private double[] lastOutputs;
    private double[][] weightsMatrix; // Neues Attribut für die Gewichtsmatrix

    public Layer(int numNeurons, int numInputsPerNeuron, ActivationFunction af) {
        this.neurons = new ArrayList<>(numNeurons);
        this.activationFunction = af;
        this.weightsMatrix = new double[numNeurons][numInputsPerNeuron];

        for (int i = 0; i < numNeurons; i++) {
            Neuron neuron = new Neuron(numInputsPerNeuron);
            this.neurons.add(neuron);
            // Gewichte direkt in der Matrix speichern
            this.weightsMatrix[i] = neuron.getWeights();
        }
    }

    public double[] feedForward(double[] inputs) {
        this.lastOutputSums = new double[neurons.size()];

        for (int i = 0; i < neurons.size(); i++) {
            lastOutputSums[i] = neurons.get(i).calculateWeightedSum(inputs);
        }

        this.lastOutputs = activationFunction.activate(lastOutputSums);
        return this.lastOutputs;
    }

    // Die backward-Methode ist jetzt nicht mehr notwendig, da die Logik in NeuralNetwork
    // für mehr Kontrolle zentralisiert wurde.

    // --- Getter-Methoden ---
    public List<Neuron> getNeurons() {
        return neurons;
    }

    public ActivationFunction getActivationFunction() {
        return activationFunction;
    }

    public double[] getLastOutputSums() {
        return lastOutputSums;
    }

    public double[] getLastOutputs() {
        return lastOutputs;
    }

    public double[][] getWeightsMatrix() {
        return weightsMatrix;
    }
}