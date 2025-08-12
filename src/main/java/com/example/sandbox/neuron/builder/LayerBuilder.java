package com.example.sandbox.neuron.builder;

import com.example.sandbox.neuron.Layer;
import com.example.sandbox.neuron.Matrix;
import com.example.sandbox.neuron.interfaces.WeightInitializer;
import com.example.sandbox.neuron.activation.ActivationFunction;
import com.example.sandbox.neuron.optimizer.Optimizer;
import com.example.sandbox.neuron.optimizer.SGDOptimizer;

import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class LayerBuilder {
    private int inputSize;
    private int outputSize;
    private Supplier<ActivationFunction> activation;
    private BiFunction<Integer, Integer, Optimizer> optimizerFactory;
    private Supplier<Optimizer> optimizerSupplier;
    private WeightInitializer weightInitializer;
    private WeightInitializer biasInitializer;
    private Supplier<Random> randomSupplier;

    public LayerBuilder() {
        // Sensible Defaults
        this.weightInitializer = Matrix::xavier;
        this.biasInitializer = Matrix::random;
        this.randomSupplier = Random::new;
    }

    // Core Configuration
    public LayerBuilder withSize(int inputSize, int outputSize) {
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        return this;
    }

    public LayerBuilder withActivation(Supplier<ActivationFunction> activation) {
        this.activation = activation;
        return this;
    }

    // Optimizer Configuration
    public LayerBuilder withOptimizerBySize(BiFunction<Integer, Integer, Optimizer> factory) {
        this.optimizerFactory = factory;
        this.optimizerSupplier = null;
        return this;
    }

    public LayerBuilder withOptimizer(Supplier<Optimizer> supplier) {
        this.optimizerSupplier = supplier;
        this.optimizerFactory = null;
        return this;
    }

    public LayerBuilder withOptimizer(Optimizer optimizer) {
        return withOptimizer(() -> optimizer);
    }

    // Weight Initialization
    public LayerBuilder withWeightInitializer(WeightInitializer initializer) {
        this.weightInitializer = initializer;
        return this;
    }

    public LayerBuilder withBiasInitializer(WeightInitializer initializer) {
        this.biasInitializer = initializer;
        return this;
    }

    // Random Configuration
    public LayerBuilder withRandomSupplier(Supplier<Random> randomSupplier) {
        this.randomSupplier = randomSupplier;
        return this;
    }

    public LayerBuilder withSeed(long seed) {
        return withRandomSupplier(() -> new Random(seed));
    }


    public LayerBuilder withRandomWeights(double scale) {
        return withWeightInitializer((rows, cols, random) ->
                Matrix.random(rows, cols, random).multiply(scale));
    }

    public Layer build() {
        validate();

        Random random = randomSupplier.get();
        ActivationFunction activationFunction = activation.get();
        Matrix weights = weightInitializer.initialize(outputSize, inputSize, random);
        Matrix biases = biasInitializer.initialize(outputSize, 1, random);

        Optimizer optimizer = resolveOptimizer();

        return new Layer(inputSize, outputSize, activationFunction, optimizer, weights, biases);
    }

    private Optimizer resolveOptimizer() {
        if (optimizerFactory != null) {
            return optimizerFactory.apply(inputSize, outputSize);
        }
        if (optimizerSupplier != null) {
            return optimizerSupplier.get();
        }
        return new SGDOptimizer(); // Default fallback
    }

    private void validate() {
        if (inputSize <= 0 || outputSize <= 0) {
            throw new IllegalStateException("Layer sizes must be positive");
        }
        if (activation == null) {
            throw new IllegalStateException("Activation function must be specified");
        }
    }
}