package com.example.sandbox.ki.transformer;

import com.example.sandbox.ki.neuron.Matrix;
import com.example.sandbox.ki.neuron.loss.LossFunction;

public class TransformerModel {
    private final Encoder encoder;
    private final Decoder decoder;
    private final PositionalEncoding positionalEncoding;
    private final LossFunction lossFunction;

    // Hyperparameter festlegen
    private final int d_model = 128;
    private final int numHeads = 8;
    private final int numLayers = 6;
    private final int d_ff = d_model * 4;
    private final int vocabSize = 30;
    private final int wordLength = 5;

    public TransformerModel(LossFunction lossFunction) {
        this.positionalEncoding = new PositionalEncoding(wordLength, d_model);
        this.encoder = new Encoder(d_model, numHeads, d_ff, numLayers);
        this.decoder = new Decoder(d_model, numHeads, d_ff, numLayers);
        this.lossFunction = lossFunction;
    }

    /**
     * Führt einen kompletten Forward-Pass des Modells aus.
     *
     * @param inputSequence  Die Eingabedaten.
     * @param outputSequence Die Ausgabedaten des Decoders.
     * @return Die Ausgabe des letzten Decoderschritts.
     */
    public Matrix forward(int[] inputSequence, int[] outputSequence) {
        // Encoder-Teil
        Matrix inputMatrix = positionalEncoding.apply(inputSequence);
        Matrix encoderOutput = encoder.forward(inputMatrix);

        // Decoder-Teil
        Matrix outputMatrix = positionalEncoding.apply(outputSequence);
        return decoder.forward(outputMatrix, encoderOutput);
    }

    /**
     * Führt einen kompletten Trainingsschritt aus: Forward-Pass und Backward-Pass.
     *
     * @param inputSequence  Die Eingabedaten (z.B. die Tokennummern des ersten Satzes).
     * @param outputSequence Die Ausgabedaten (z.B. die Tokennummern des zu generierenden Satzes).
     * @param expectedOutput Die erwarteten Ausgabedaten für die Verlustberechnung.
     * @param learningRate   Die Lernrate für den Optimierer.
     */
    public void train(int[] inputSequence, int[] outputSequence, Matrix expectedOutput, double learningRate) {
        // --- 1. Forward-Pass ---
        // Die forward-Methode des Modells wird aufgerufen,
        // um die Ausgabematrix zu erhalten.
        Matrix finalOutput = forward(inputSequence, outputSequence);

        // --- 2. Backward-Pass ---
        // Berechne den initialen Fehler-Gradienten des Ausgabelayers
        Matrix outputLossDerivative = lossFunction.derivative(expectedOutput, finalOutput);

        // Propagiere den Gradienten durch den Decoder
        // Die backward-Methode des Decoders gibt das Delta für den Encoder zurück
        Matrix encoderOutput = encoder.forward(positionalEncoding.apply(inputSequence));
        Matrix deltaFromDecoder = decoder.backward(outputLossDerivative, encoderOutput);

        // Propagiere den Gradienten durch den Encoder
        encoder.backward(deltaFromDecoder);

        // Optional: Hier könnten auch die Parameter der PositionalEncoding und des Embeddings geupdatet werden.
    }
}