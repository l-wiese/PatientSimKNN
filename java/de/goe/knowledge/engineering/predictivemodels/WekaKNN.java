package de.goe.knowledge.engineering.predictivemodels;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.DistanceFunction;
import weka.core.Instances;

public class WekaKNN {

	DistanceFunction distance;
	int k;
	String trainingFile;
	String testFile;
	Instances trainingData;
	Instances testData;
	
	
	public WekaKNN(int k, DistanceFunction distance, String trainingFile, String testFile) {
		this.distance = distance;
		this.k = k;
		this.trainingData = null;
		this.testData = null;
		this.trainingFile = trainingFile;
		this.testFile = testFile;
	}
	
	public WekaKNN(int k, DistanceFunction distance, Instances trainingData, Instances testData) {
		this.distance = distance;
		this.k = k;
		this.trainingData = trainingData;
		this.testData = testData;
		this.trainingFile = null;
		this.testFile = null;
	}

	/**
	 * Runs the kNN algorithm using Weka.
	 * @return The result as 2x2 confusion matrix
	 */
	public double[][] run() {
		FileReader trainingReader, testReader;
		Evaluation eval = null;
		try {
			Instances training = trainingData, test = testData;
			if (trainingFile != null) {
				trainingReader = new FileReader(this.trainingFile);
				training = new Instances(trainingReader);
			}
			if (testFile != null) {
				testReader = new FileReader(this.testFile);
				test = new Instances(testReader);
			}
			// last column is the classifier
			training.setClassIndex(training.numAttributes() - 1);
			test.setClassIndex(test.numAttributes() - 1);
			
			IBk ibk = new IBk();
			ibk.setKNN(k);
			ibk.getNearestNeighbourSearchAlgorithm().setDistanceFunction(distance);
			ibk.buildClassifier(training);
			eval = new Evaluation(training);
			eval.evaluateModel(ibk, test);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return eval.confusionMatrix();
	}
}
