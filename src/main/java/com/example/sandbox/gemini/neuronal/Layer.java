package com.example.sandbox.gemini.neuronal;

import com.example.sandbox.gemini.neuronal.activation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Layer {
    private final List<Neuron> neurons;
    private final ActivationFunction activationFunction;
    private double[] lastOutputSums;
    private double[] lastOutputs;

    // Standard-Konstruktor ohne Seed (Zufall per Systemzeit)
    public Layer(int numNeurons, int numInputsPerNeuron, ActivationFunction af) {
        this(numNeurons, numInputsPerNeuron, af, null);
    }

    // Konstruktor mit optionalem Seed für reproduzierbare Runs
    public Layer(int numNeurons, int numInputsPerNeuron, ActivationFunction af, Random rng) {
        this.neurons = new ArrayList<>(numNeurons);
        this.activationFunction = af;

        Neuron.InitType initType = chooseInitType(af);

        for (int i = 0; i < numNeurons; i++) {
            this.neurons.add(new Neuron(numInputsPerNeuron, rng, initType));
//            this.neurons.add(new Neuron(numInputsPerNeuron, rng, Neuron.InitType.UNIFORM));
        }
    }

    /**
     * Wählt automatisch den passenden Initialisierungstyp
     * basierend auf der Aktivierungsfunktion.
     */
    private Neuron.InitType chooseInitType(ActivationFunction af) {
        return switch (af) {
            // He-Init für ReLU-ähnliche
            case ReLU r      -> Neuron.InitType.HE;
            case LeakyReLU l -> Neuron.InitType.HE;
            case ELU e       -> Neuron.InitType.HE;
            case GELU g      -> Neuron.InitType.HE;

            // Xavier für Sigmoid/Tanh-artige oder Softmax
            case Sigmoid s   -> Neuron.InitType.XAVIER;
            case Softmax sm  -> Neuron.InitType.XAVIER;
            case Tanh t      -> Neuron.InitType.XAVIER;

            // Fallback: gleichverteilte Werte
            default          -> Neuron.InitType.UNIFORM;
        };
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
