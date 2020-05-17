package org.neuroph.samples.standard10ml;

import java.util.Arrays;
import java.util.List;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.error.MeanSquaredError;
import org.neuroph.eval.ClassifierEvaluator;
import org.neuroph.eval.ErrorEvaluator;
import org.neuroph.eval.Evaluation;
import org.neuroph.eval.classification.ClassificationMetrics;
import org.neuroph.eval.classification.ConfusionMatrix;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.data.norm.MaxNormalizer;
import org.neuroph.util.data.norm.Normalizer;

/**
 *
 * 
 */
/*
 INTRODUCTION TO THE PROBLEM AND DATA SET INFORMATION:
 1. Data set that will be used in this experiment: Wine Quality Dataset
    The Wine Quality Dataset involves predicting the quality of white wines on a scale given chemical measures of each wine.
    It is a multi-class classification problem, but could also be framed as a regression problem.
    The original data set that will be used in this experiment can be found at link:
    http://archive.ics.uci.edu/ml/machine-learning-databases/wine-quality/winequality-white.csv
2. Reference:  National Institute of Diabetes and Digestive and Kidney Diseases
   Paulo Cortez, University of Minho, Guimarães, Portugal, http://www3.dsi.uminho.pt/pcortez
   A. CeA. Cerdeira, F. Almeida, T. Matos and J. Reis, Viticulture Commission of the Vinho Verde Region(CVRVV), Porto, Portugal , @ 2009
3. Number of instances: 4 898
4. Number of Attributes: 11 pluss class attributes (inputs are continuous aand numerical values, and output is numerical)
5. Attribute Information:
 Inputs:
 11 attributes:
 11 numerical or continuous features are computed for each wine:
 1) Fixed acidity.
 2) Volatile acidity.
 3) Citric acid.
 4) Residual sugar.
 5) Chlorides.
 6) Free sulfur dioxide.
 7) Total sulfur dioxide.
 8) Density.
 9) pH.
 10) Sulphates.
 11) Alcohol.
 12) Output: Quality (score between 0 and 10).
6. Missing Values: None.
 */
public class WineQualityClassification {

    public static void main(String[] args) {
        (new WineQualityClassification()).run();
    }

    public void run() {
        System.out.println("Creating data set...");
        String dataSetFile = "data_sets/ml10standard/wine.txt";
        int inputsCount = 11;
        int outputsCount = 10;

        // create data set from file
        DataSet dataSet = DataSet.createFromFile(dataSetFile, inputsCount, outputsCount, "\t", true);

        // split data into train and test set
        DataSet[] trainTestSplit = dataSet.split(0.6, 0.4);
        DataSet trainingSet = trainTestSplit[0];
        DataSet testSet = trainTestSplit[1];

        Normalizer norm = new MaxNormalizer(trainingSet);
        norm.normalize(trainingSet);
        norm.normalize(testSet);

        System.out.println("Creating neural network...");
        MultiLayerPerceptron neuralNet = new MultiLayerPerceptron(inputsCount, 20, 15, outputsCount);

        neuralNet.setLearningRule(new MomentumBackpropagation());
        MomentumBackpropagation learningRule = (MomentumBackpropagation) neuralNet.getLearningRule();
        learningRule.addListener((event)->{
            MomentumBackpropagation bp = (MomentumBackpropagation) event.getSource();
            System.out.println(bp.getCurrentIteration() + ". iteration | Total network error: " + bp.getTotalNetworkError());
        });

        // set learning rate and max error
        learningRule.setLearningRate(0.1);
        learningRule.setMaxIterations(5000);

        System.out.println("Training network...");
        // train the network with training set
        neuralNet.learn(trainingSet);
        System.out.println("Training completed.");
        System.out.println("Testing network...");

        System.out.println("Network performance on the test set");
        evaluate(neuralNet, testSet);

        System.out.println("Saving network");
        // save neural network to file
        neuralNet.save("nn1.nnet");

        System.out.println("Done.");

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
            System.out.println("Output: " + Arrays.toString(networkOutput));
            System.out.println("Desired output" + Arrays.toString(testSetRow.getDesiredOutput()));
        }
    }

    // Evaluates performance of neural network.
    // Contains calculation of Confusion matrix for classification tasks or Mean Ssquared Error and Mean Absolute Error for regression tasks.
    // Difference in binary and multi class classification are made when adding Evaluator (MultiClass or Binary).
    public void evaluate(NeuralNetwork neuralNet, DataSet dataSet) {

        System.out.println("Calculating performance indicators for neural network.");

        Evaluation evaluation = new Evaluation();
        evaluation.addEvaluator(new ErrorEvaluator(new MeanSquaredError()));

        String classLabels[] = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        evaluation.addEvaluator(new ClassifierEvaluator.MultiClass(classLabels));
        evaluation.evaluate(neuralNet, dataSet);

        ClassifierEvaluator evaluator = evaluation.getEvaluator(ClassifierEvaluator.MultiClass.class);
        ConfusionMatrix confusionMatrix = evaluator.getResult();
        System.out.println("Confusion matrrix:\r\n");
        System.out.println(confusionMatrix.toString() + "\r\n\r\n");
        System.out.println("Classification metrics\r\n");
        ClassificationMetrics[] metrics = ClassificationMetrics.createFromMatrix(confusionMatrix);
        ClassificationMetrics.Stats average = ClassificationMetrics.average(metrics);
        for (ClassificationMetrics cm : metrics) {
            System.out.println(cm.toString() + "\r\n");
        }
        System.out.println(average.toString());
    }

}
