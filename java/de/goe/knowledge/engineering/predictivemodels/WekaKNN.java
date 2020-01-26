package de.goe.knowledge.engineering.predictivemodels;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.DistanceFunction;
import weka.core.Instances;

public class WekaKNN {

	DistanceFunction distance;
	int k;
	String file;

	public WekaKNN(int k, DistanceFunction distance, String file) {
		this.distance = distance;
		this.k = k;
		this.file = file;
	}

	public double[][] run() {
		FileReader reader;
		Evaluation eval = null;
		try {
			reader = new FileReader("data/" + file);

			Instances data = new Instances(reader);
			// last column is the classifier
			data.setClassIndex(data.numAttributes() - 1);

			IBk ibk = new IBk();
			ibk.setKNN(k);
			ibk.getNearestNeighbourSearchAlgorithm().setDistanceFunction(distance);

			eval = new Evaluation(data);
			Random rand = new Random(1); // using seed = 1
			int folds = 10;
			eval.crossValidateModel(ibk, data, folds, rand);
			System.out.println(eval.toMatrixString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return eval.confusionMatrix();
	}

	// public void runSingle() {
	// FileReader reader;
	// try {
	// reader = new FileReader("data/" + file);
	//
	// Instances data = new Instances(reader);
	// // last column is the classifier
	// data.setClassIndex(data.numAttributes() - 1);
	//
	// // Adapt to Cross-Fold-Validation
	// Instance first = data.instance(0);
	// data.delete(0);
	// Instances test = new Instances(data, data.numInstances() - 32634);
	//
	// IBk ibk = new IBk();
	// ibk.getNearestNeighbourSearchAlgorithm().setDistanceFunction(distance);
	// ibk.setKNN(k);
	// long startTime = System.nanoTime();
	// ibk.buildClassifier(test);
	// ibk.classifyInstance(first);
	// long eucStopTime = System.nanoTime();
	// long eucDuration = eucStopTime - startTime;
	// System.out.println("This took " + eucDuration + " nanosec");
	//
	// // 726797900, 753460299, 711180800
	// System.out.println(first.toString(0) + ": " + ibk.classifyInstance(first));
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
}
