package org.neuroph.samples.standard10ml;

import java.util.Arrays;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.learning.error.MeanAbsoluteError;
import org.neuroph.core.learning.error.MeanSquaredError;
import org.neuroph.nnet.Adaline;
import org.neuroph.nnet.learning.LMS;
import org.neuroph.util.data.norm.MaxNormalizer;
import org.neuroph.util.data.norm.Normalizer;

/**
 *
 * 
 */
/*
 INTRODUCTION TO THE PROBLEM AND DATA SET INFORMATION:
 1. Data set that will be used in this experiment: Swedish Auto Insurance Dataset
    The Swedish Auto Insurance Dataset involves predicting the total payment for all claims in thousands of Swedish Kronor, given the total number of claims.
    The original data set that will be used in this experiment can be found at link:
    https://www.math.muni.cz/~kolacek/docs/frvs/M7222/data/AutoInsurSweden.txt
2. Reference: Swedish Committee on Analysis of Risk Premium in Motor Insurance
3. Number of instances: 63
4. Number of Attributes: 2 (input is numerical, output is continuous)
5. Attribute Information:
   In the following data
   X = number of claims (numerical)
   Y = total payment for all the claims in thousands of Swedish Kronor (continuous) for geographical zones in Sweden.
6. Missing Values: none
 */
public class SwedishAutoInsurance {

    public static void main(String[] args) {
        (new SwedishAutoInsurance()).run();
    }

    public void run() {
        System.out.println("Creating data set...");
        String dataSetFile = "data_sets/ml10standard/autodata.txt";
        int inputsCount = 1;
        int outputsCount = 1;

        // create data set from file
        DataSet dataSet = DataSet.createFromFile(dataSetFile, inputsCount, outputsCount, ",");

        // split data into train and test set
        DataSet[] trainTestSplit = dataSet.split(0.6, 0.4);
        DataSet trainingSet = trainTestSplit[0];
        DataSet testSet = trainTestSplit[1];

        // normalize training and test set
        Normalizer norm = new MaxNormalizer(trainingSet);
        norm.normalize(trainingSet);
        norm.normalize(testSet);

        System.out.println("Creating neural network...");
        Adaline neuralNet = new Adaline(1);
        LMS learningRule = (LMS) neuralNet.getLearningRule();
        learningRule.addListener((event) -> {
            LMS bp = (LMS) event.getSource();
            System.out.println(bp.getCurrentIteration() + ". iteration | Total network error: " + bp.getTotalNetworkError());
        });

        // train the network with training set
        System.out.println("Training network...");
        neuralNet.learn(trainingSet);
        System.out.println("Training completed.");

        System.out.println("Testing network...");
        System.out.println("Network performance on the test set");
        evaluate(neuralNet, testSet);

        System.out.println("Saving trained network");
        // save neural network to file
        neuralNet.save("nn1.nnet");

        System.out.println();
        System.out.println("Network outputs for test set");
        testNeuralNetwork(neuralNet, testSet);
    }

    // Displays inputs, desired output (from dataset) and actual output (calculated by neural network) for every row from data set.
    public void testNeuralNetwork(NeuralNetwork neuralNet, DataSet testSet) {

        System.out.println("Showing inputs, desired output and neural network output for every row in test set.");

        for (DataSetRow testSetRow : testSet.getRows()) {
            neuralNet.setInput(testSetRow.getInput());
            neuralNet.calculate();
            double[] networkOutput = neuralNet.getOutput();

            System.out.println("Input: " + Arrays.toString(testSetRow.getInput()));
            System.out.println("Output: " + networkOutput[0]);
            System.out.println("Desired output" + Arrays.toString(networkOutput));
        }
    }

    // Evaluates performance of neural network.
    // Contains calculation of Confusion matrix for classification tasks or Mean Ssquared Error and Mean Absolute Error for regression tasks.
    // Difference in binary and multi class classification are made when adding Evaluator (MultiClass or Binary).
    public void evaluate(NeuralNetwork neuralNet, DataSet dataSet) {

        System.out.println("Calculating performance indicators for neural network.");

        MeanSquaredError mse = new MeanSquaredError();
        MeanAbsoluteError mae = new MeanAbsoluteError();

        for (DataSetRow testSetRow : dataSet.getRows()) {
            neuralNet.setInput(testSetRow.getInput());
            neuralNet.calculate();
            double[] networkOutput = neuralNet.getOutput();
            double[] desiredOutput = testSetRow.getDesiredOutput();
            mse.addPatternError(networkOutput, desiredOutput);
            mae.addPatternError(networkOutput, desiredOutput);
        }

        System.out.println("Mean squared error is: " + mse.getTotalError());
        System.out.println("Mean absolute error is: " + mae.getTotalError());
    }

}
