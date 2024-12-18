package game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

// Matrix class for each layer (input to next layer transformation)
class Matrix {
    ArrayList<Float> input;
    ArrayList<ArrayList<Float>> weights;
    ArrayList<Float> output;
    Random random = new Random();

    public Matrix(int inputSize, int outputSize) {
        input = new ArrayList<>(inputSize);
        output = new ArrayList<>(outputSize);
        weights = new ArrayList<>();
        
        // Initialize weights using Xavier initialization
        for (int i = 0; i < outputSize; i++) {
            ArrayList<Float> row = new ArrayList<>();
            for (int j = 0; j < inputSize; j++) {
                row.add(xavierInit(inputSize));
            }
            weights.add(row);
        }
    }

    // Xavier Initialization: Random values with mean 0 and variance 1/n
    private float xavierInit(int inputSize) {
        return (float)(random.nextGaussian() * Math.sqrt(1.0 / inputSize));
    }

    // Sigmoid activation function
    float sigmoid(float x) {
        return 1 / (1 + (float) Math.exp(-x));
    }

    // Perform matrix multiplication and apply sigmoid activation
    void forwardPass(ArrayList<Float> inputs) {
        input = inputs;
        output.clear(); // Clear previous outputs
        for (ArrayList<Float> weightRow : weights) {
            float sum = 0;
            // Compute dot product: input * weights[i]
            for (int i = 0; i < input.size(); i++) {
                sum += input.get(i) * weightRow.get(i);
            }
            // Apply sigmoid activation to the sum
            output.add(sigmoid(sum));
        }
    }

    // Return the output after the forward pass
    ArrayList<Float> getOutput() {
        return output;
    }

    // Helper function to set input for testing
    void setInput(float... values) {
        input.clear();
        for (float v : values) {
            input.add(v);
        }
    }
}

// Neural network class with multiple layers
class NeuralNetwork {
    ArrayList<Matrix> layers; // List of layers (each layer is a matrix)
    int[] layerSizes; // Sizes of each layer (input, hidden, output)

    public NeuralNetwork(int... sizes) {
        layerSizes = sizes;
        layers = new ArrayList<>();
        // Create layers
        for (int i = 0; i < sizes.length - 1; i++) {
            layers.add(new Matrix(sizes[i], sizes[i + 1]));
        }
    }

    // Perform forward propagation through the entire network
    ArrayList<Float> forward(ArrayList<Float> input) {
        ArrayList<Float> currentInput = input;
        // Pass through each layer
        for (Matrix layer : layers) {
            layer.forwardPass(currentInput); // Perform forward pass for each layer
            currentInput = layer.getOutput(); // The output of this layer becomes input to the next layer
        }
        return currentInput; // Final output from the last layer
    }

    // Helper function to set the input and print the output for testing
    ArrayList<Float> testNetwork(float... inputValues) {
        ArrayList<Float> input = new ArrayList<>();
        for (float v : inputValues) {
            input.add(v);
        }
    
        // Forward pass
        ArrayList<Float> output = forward(input);

        // Return the result (final output)
        return output;
    }
    // Function to vary weights slightly by adding a small random perturbation
    void perturbWeights(float perturbationMagnitude) {
        Random random = new Random();

        // Iterate over each layer in the network
        for (Matrix layer : layers) {
            // Iterate over each neuron (output unit) in the layer
            for (ArrayList<Float> neuronWeights : layer.weights) {
                // Iterate over each weight in the neuron
                for (int i = 0; i < neuronWeights.size(); i++) {
                    // Add a small random perturbation to each weight
                    float perturbation = (random.nextFloat() - 0.5f) * 2 * perturbationMagnitude; // Range: [-magnitude, magnitude]
                    neuronWeights.set(i, neuronWeights.get(i) + perturbation);
                }
            }
        }
    }
    public void recordWeights(String filename) {
            try (FileWriter writer = new FileWriter(filename)) {
                for (int layer = 0; layer < layers.size(); layer++) {
                    writer.write("Layer " + layer + " weights:\n");
                    ArrayList<ArrayList<Float>> layerWeights = layers.get(layer).weights;
                    
                    for (int i = 0; i < layerWeights.size(); i++) {
                        writer.write("Neuron " + i + ": ");
                        ArrayList<Float> neuronWeights = layerWeights.get(i);
                        for (float weight : neuronWeights) {
                            writer.write(weight + " ");
                        }
                        writer.write("\n");
                    }
                    writer.write("\n");
                }
                //System.out.println("Weights successfully saved to " + filename);
            } catch (IOException e) {
                //System.out.println("An error occurred while saving the weights.");
                e.printStackTrace();
            }
        }
    // Function to load the weights from a .txt file
    public void loadWeights(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int layerIndex = 0;
            int neuronIndex = 0;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Layer")) {
                    layerIndex = Integer.parseInt(line.split(" ")[1]); // Extract the layer number
                    neuronIndex = 0; // Reset neuron index for each layer
                } else if (line.startsWith("Neuron")) {
                    // Read the weights for this neuron
                    Scanner scanner = new Scanner(line.split(":")[1].trim());
                    ArrayList<Float> neuronWeights = layers.get(layerIndex).weights.get(neuronIndex);
                    for (int i = 0; i < neuronWeights.size(); i++) {
                        if (scanner.hasNextFloat()) {
                            neuronWeights.set(i, scanner.nextFloat()); // Set weight value
                        }
                    }
                    scanner.close();
                    neuronIndex++;
                }
            }
            //System.out.println("Weights successfully loaded from " + filename);
        } catch (IOException e) {
            //System.out.println("An error occurred while reading the weights.");
            //e.printStackTrace();
        }
    }
    // Function to copy weights from another neural network
    void copyWeights(NeuralNetwork source) {
        if (this.layers.size() != source.layers.size()) {
            //System.out.println("Error: Neural networks must have the same architecture.");
            return;
        }

        // Iterate over layers and copy weights
        for (int layer = 0; layer < this.layers.size(); layer++) {
            Matrix targetLayer = this.layers.get(layer);
            Matrix sourceLayer = source.layers.get(layer);

            if (targetLayer.weights.size() != sourceLayer.weights.size()) {
                //System.out.println("Error: Mismatched number of neurons in layer " + layer);
                return;
            }

            // Copy weights neuron by neuron
            for (int neuron = 0; neuron < targetLayer.weights.size(); neuron++) {
                ArrayList<Float> targetNeuronWeights = targetLayer.weights.get(neuron);
                ArrayList<Float> sourceNeuronWeights = sourceLayer.weights.get(neuron);

                if (targetNeuronWeights.size() != sourceNeuronWeights.size()) {
                    //System.out.println("Error: Mismatched number of weights for neuron " + neuron + " in layer " + layer);
                    return;
                }

                // Copy individual weights
                for (int weight = 0; weight < targetNeuronWeights.size(); weight++) {
                    targetNeuronWeights.set(weight, sourceNeuronWeights.get(weight));
                }
            }
        }

        //System.out.println("Weights successfully copied.");
    }
}

public class NeuralNetworkExample {
    public static void main(String[] args) {
        // Create a network with 3 inputs, 2 neurons in the hidden layer, and 1 output
        NeuralNetwork neuralNet = new NeuralNetwork(3, 6, 36, 6, 3, 1);

        // Set the input and run the forward pass through the network
        neuralNet.testNetwork(1.0f, 0.5f, -1.2f);
    }
}
