package com.example.sandbox.neuronal;

import java.util.List;

public class NeuralNetwork {
    private Layer hiddenLayer;
    private Layer outputLayer;

    public NeuralNetwork(int inputSize, int hiddenSize, int outputSize) {
        this.hiddenLayer = new Layer(inputSize, hiddenSize);
        this.outputLayer = new Layer(hiddenSize, outputSize);
    }

    public List<Double> predict(List<Double> inputs) {
        List<Double> hiddenOutputs = hiddenLayer.forward(inputs);
        return outputLayer.forward(hiddenOutputs);
    }

    public void train(List<List<Double>> trainingInputs, List<List<Double>> trainingTargets, int epochs, double learningRate) {
        for (int e = 0; e < epochs; e++) {
            for (int i = 0; i < trainingInputs.size(); i++) {
                List<Double> inputs = trainingInputs.get(i);
                List<Double> targets = trainingTargets.get(i);

                // Vorwärtsdurchlauf
                List<Double> hiddenOutputs = hiddenLayer.forward(inputs);
                List<Double> finalOutputs = outputLayer.forward(hiddenOutputs);

                // Rückwärtsdurchlauf (Backpropagation)
                outputLayer.backward(finalOutputs, targets, hiddenOutputs, learningRate);
                hiddenLayer.backward(hiddenOutputs, outputLayer.getDeltas(), inputs, learningRate, outputLayer.getWeights());
            }
        }

        hiddenLayer.getWeights().forEach(System.out::println);
        System.out.println("---");
        outputLayer.getWeights().forEach(System.out::println);
    }
}