package com.example.sandbox.gemini.neuronal;

import com.example.sandbox.gemini.neuronal.activation.ActivationFunction;

import java.util.ArrayList;
import java.util.List;

public class Layer {
    private final List<Neuron> neurons;
    private final ActivationFunction activationFunction;
    private double[] lastOutputSums;
    private double[] lastOutputs;

    public Layer(int numNeurons, int numInputsPerNeuron, ActivationFunction af) {
        this.neurons = new ArrayList<>(numNeurons);
        this.activationFunction = af;

        for (int i = 0; i < numNeurons; i++) {
            Neuron neuron = new Neuron(numInputsPerNeuron);
            this.neurons.add(neuron);
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

    /**
     * Liefert die aktuelle Gewichtsmatrix, abgeleitet von den Neuronen.
     * Jede Zeile entspricht den Gewichten eines Neurons.
     */
    public double[][] getWeightsMatrix() {
        double[][] matrix = new double[neurons.size()][];
        for (int i = 0; i < neurons.size(); i++) {
            // Direkte Referenz auf das Array – falls du Schutz willst, hier .clone() verwenden
            matrix[i] = neurons.get(i).getWeights();
        }
        return matrix;
    }

    // --- Getter ---
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
}
