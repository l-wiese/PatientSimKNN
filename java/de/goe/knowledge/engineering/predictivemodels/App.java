package de.goe.knowledge.engineering.predictivemodels;

import java.util.ArrayList;


import de.lmu.ifi.dbs.elki.distance.distancefunction.CosineDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.ManhattanDistanceFunction;
import de.lmu.ifi.dbs.elki.evaluation.classification.ConfusionMatrix;
import weka.core.EuclideanDistance;
import weka.core.ManhattanDistance;
import weka.core.Instances;


/**
 * Implementing a new similarity (or distance) interface for ELKI and Apache
 * Mahout that can retrieve pairwise similarity (or distance) values for each
 * pair of patients from a similarity (or distance) table stored in a database
 * system.
 * 
 * Choosing a prediction model (like nearest-neighbor-learning, logistic
 * regression or decision tree) available in ELKI and Mahout to either predict
 * the mortality of an index patient or the disease (ICD-9 code) of an index
 * patient based on other features of the patient.
 * 
 * Testing the effect of different similarity (or distance) measures (like
 * Cosine, Euclidean Mahalanobis) on the accuracy and the AUROC of the
 * prediction (using the previously developed similarity interface).
 * 
 * Testing the effect of training the prediction model on a cohort of similar
 * patients (as opposed to training on the entire data set) on the accuracy and
 * the AUROC of the prediction.
 * 
 * DEAD = 4284 (TN + FP) ALIVE = 28351 (TP + FN)
 */


public class App {

	static int numberOfAttributes = 0;
	static enum modes {Undersample, Bootstrap, SMOTE, NoResampling};
	
	// Distance functions
	static String[] evalName = {"ELKI Euclidean", "ELKI Manhattan",
								"ELKI Cosine", "Weka Euclidean",
								"Weka Manhattan", "Weka Cosine"};
	static de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction<?>[]
			distFunctionElki = {EuclideanDistanceFunction.STATIC,
								ManhattanDistanceFunction.STATIC,
								CosineDistanceFunction.STATIC};
	static weka.core.DistanceFunction[] 
			distFunctionWeka = {/*new EuclideanDistance(),
								new ManhattanDistance(),
								new WekaCosineDistance()*/};

	// Command-line arguments
	static double evalDelta = 0.001d;
	static int evalNDelta = 5; // n_delta: Number of times the evaluation delta has to be smaller than epsilon in a row to finish
	static int[] kVals = { 5 };
	static int resumeFrom = 0; // ID of the distance function to resume from (in case of crashes).
	static boolean verbose = false;
	static boolean display = false;
	static boolean saveOversampledDatasets = false;
	static modes mode = modes.Bootstrap;
	static int randomSeed = 1;
	
	// Main files
	static String inputFile = "data/weka_shortened_test2.arff";
	static String outputFile= "results/results.csv";
	
	
	// Option used for debugging
	static int measurementId = 0;
	
	
	public static void main(String[] args) throws Exception {
		// Parse input args
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
				case "-i":
				case "--input":
					inputFile = args[++i];
					break;
				
				case "-o":
				case "--output":
					outputFile = args[++i];
					break;
				
				case "-d":
				case "--delta":
					evalDelta = Double.parseDouble(args[++i]);
					break;
				
				case "-n":
				case "--nd":
				case "--ndelta":
					evalNDelta = Integer.parseInt(args[++i]);
					break;
					
				case "--resumefrom":
					resumeFrom = Integer.parseInt(args[++i]);
					break;
				
				case "--verbose":
					verbose = true;
					break;
				
				case "--display":
					display = true;
					break;
					
				case "--saveoversampleddatasets":
					saveOversampledDatasets = true;
					break;
				
				case "-m":
				case "--mode":
					String arg = args[++i].replace("\n", "").replace("\r", "");
					int argi = Integer.parseInt(args[i]);
					if (arg == "smote" || argi == 1) {
						mode = modes.SMOTE;
						System.out.println ("Mode: SMOTE");
						break;
					}
					if (arg == "undersample" || argi == 2) {
						mode = modes.Undersample;
						System.out.println ("Mode: Undersampling");
						break;
					}
					if (arg == "none" || argi == 3) {
						mode = modes.NoResampling;
						System.out.println ("Mode: No resampling");
						break;
					}
					System.out.println ("Mode: Oversampling");
					break;
				
				case "-k":
					kVals = new int[]{Integer.parseInt(args[++i])};
					break;
					
				case "-s":
				case "--seed":
					randomSeed = Integer.parseInt(args[++i]);
					break;
				
				default:
					System.out.println ("Unknown parameter " + args[i] + "\nAborting...");
					System.exit(1);
			}
		}
		// Analyzer initialization
		Analyzer analyzer = new Analyzer();
		
		
		// CV parameters
		int numberOfPartitions = 10;
		boolean resampleExistingEntries = true;
		double ratioAlive = 1d;
		BootstrapCrossValidator cv = new BootstrapCrossValidator(InstancesHelper.loadInstancesFromFile(inputFile), numberOfPartitions, resampleExistingEntries, ratioAlive);
		cv.partition();
		numberOfAttributes = cv.getThisFold()[0].numAttributes();
		
		// Misc. initialization
		double time;
		measurementId = 0;

		// Main Loop
		for (int i = 0; i < kVals.length; i++) {
	//		// KNN ELKI
			System.out.println("ELKI");
			for (int j = 0; j < distFunctionElki.length; j++) {
				if (j < resumeFrom) {
					continue;
				}
				kNNEvaluation evaluation;
				System.out.println("___ " + evalName[j] + " ___");
				do { // This do-while repeats until sufficiently small delta in stat. values has been reached
					measurementId++;
					System.out.print ("Measurement " + measurementId + ": " + evalName[j]);
					evaluation = new kNNEvaluation(evalName[j]);
					analyzer.addEvaluation(evaluation);
					cv.reset();
					String trainingFile = "data/folds/" + (measurementId < 100?"0":"") + (measurementId < 10?"0":"") + measurementId + "-" + evalName[j] + "-" + cv.getCurrentFoldTrainingFilePath();
					String testFile = "data/folds/" + (measurementId < 100?"0":"") + (measurementId < 10?"0":"") + measurementId + "-" + evalName[j] + "-" + cv.getCurrentFoldTestFilePath();
					
					if (!saveOversampledDatasets) {
						trainingFile = "data/folds/training";
						testFile = "data/folds/test";
					}

					String testFileClassless = testFile + "-noclass";
					while (cv.hasNext()) {
						randomSeed++;
						long startTime = System.nanoTime();
//						if (verbose) {
//							System.out.println ("current fold: " + cv.getCurrentFold());
//						}
						System.out.print(", " + cv.getCurrentFold());
						cv.getNextFold();
						
						// Save folds to file
						cv.saveFoldToArff(trainingFile + ".arff", testFile + ".arff");
						
						// Transform to ELKI readable format
						long deltaTime = System.nanoTime() - startTime;
						CSVTransformer csvt = new CSVTransformer();
						csvt.CSVTransformerWeka2Elki(false, true,
								trainingFile + ".arff",
								trainingFile + ".csv",
								getFeatures(new ArrayList<String>()));
						csvt.CSVTransformerWeka2Elki(false, true, testFile + ".arff",
								testFile + ".csv", 
								getFeatures(new ArrayList<String>()));

						// Cut out the time transformation took (since this is a process not native to ELKI)
						startTime -= (deltaTime); 
						
						
						// Init & run algo
						ElkiKNN<Object> knnE = new ElkiKNN<Object>(kVals[i],
																	distFunctionElki[j],
																	trainingFile + ".csv",
																	testFile + ".csv",
																	testFileClassless + ".csv");
						ConfusionMatrix m = knnE.run();
						time = timeStopper(kVals[i], startTime);
						kNNEvaluation result = new kNNEvaluation(evalName[j] + "(it)", m.truePositives(0), m.falseNegatives(0), m.falsePositives(0),
								m.trueNegatives(0), kVals[i], time);
//						result.print();
						evaluation.addValuesToEvaluation(result);
						// Try to lose all ELKI references to clean up memory
						knnE = null;
						System.gc();

					}
					System.out.println();
					
					if (verbose) {
						System.out.println ("Evaluation result: ");
						evaluation.print();
					}
				} while(!analyzer.checkEvaluationDelta(evaluation, evalDelta, evalNDelta));
				
			}
			// KNN WEKA
			System.out.println("WEKA");
			for (int j = 0; j < distFunctionWeka.length; j++) {
				if (j + distFunctionElki.length < resumeFrom) {
					continue;
				}
				System.out.println("___ " + evalName[j+distFunctionElki.length] + " ___");
				kNNEvaluation evaluation;
				do { // This do-while repeats until sufficiently small delta in stat. values has been reached
					measurementId++;
					System.out.print ("Measurement " + measurementId + ": " + evalName[j+distFunctionElki.length]);
					cv.reset();	
					evaluation = new kNNEvaluation(evalName[j+distFunctionElki.length]);
					analyzer.addEvaluation(evaluation);
					String trainingFile = "data/folds/" + (measurementId < 100?"0":"") + (measurementId < 10?"0":"") + measurementId + "-" + evalName[j+distFunctionElki.length] + "-" + cv.getCurrentFoldTrainingFilePath();
					String testFile = "data/folds/" + (measurementId < 100?"0":"") + (measurementId < 10?"0":"") + measurementId + "-" + evalName[j+distFunctionElki.length] + "-" + cv.getCurrentFoldTestFilePath();
					if (!saveOversampledDatasets) {
						trainingFile = "data/folds/training";
						testFile = "data/folds/test";
					}
					
					while (cv.hasNext()) {
						randomSeed++;
						long startTime = System.nanoTime();
//						if (verbose) {
//							System.out.println ("current fold: " + cv.getCurrentFold());
//						}
						System.out.print(", " + cv.getCurrentFold());
						Instances[] fold = cv.getNextFold();
						
						// Save folds to file
						if (saveOversampledDatasets) {
							cv.saveFoldToArff(trainingFile + ".arff", testFile + ".arff");
						}
						
						// Init & run algo
						WekaKNN knnW = new WekaKNN(kVals[i], distFunctionWeka[j], fold[0], fold[1]);
						
						double[][] matrix = knnW.run();
						time = timeStopper(kVals[i], startTime);
			
						kNNEvaluation result = new kNNEvaluation(evalName[j+distFunctionElki.length] + "(it)" + fold, matrix[0][0], matrix[0][1], matrix[1][0], matrix[1][1], kVals[i], time);
//						result.print();
						evaluation.addValuesToEvaluation(result);
						knnW = null;
						System.gc();
						
					}
					System.out.println();
					
					if (verbose) {
						evaluation.print();
						System.out.println ("Evaluation result: ");
					}
				} while(!analyzer.checkEvaluationDelta(evaluation, evalDelta, evalNDelta));
			}

		}
		
		if (display) {
			analyzer.displayChart(); // Display frame with bar chart of the results
		}
		System.out.println(analyzer.getResultsAsHumanReadableString(verbose));
		analyzer.saveAsCSV(outputFile);
}


	public static ArrayList<String> getFeatures(ArrayList<String> featureList) {
		featureList.add("urine_6h");
		featureList.add("urine_12h");
		featureList.add("urine_18h");
		featureList.add("urine_24h");
		featureList.add("hr_12h_max");
		featureList.add("hr_18h_max");
		featureList.add("hr_24h_max");
		featureList.add("hr_6h_max");
		featureList.add("map_12h_max");
		featureList.add("map_18h_max");
		featureList.add("map_24h_max");
		featureList.add("map_6h_max");
		featureList.add("rr_12h_max");
		featureList.add("rr_18h_max");
		featureList.add("rr_24h_max");
		featureList.add("rr_6h_max");
		featureList.add("sbp_12h_max");
		featureList.add("sbp_18h_max");
		featureList.add("sbp_24h_max");
		featureList.add("sbp_6h_max");
		featureList.add("spo2_12h_max");
		featureList.add("spo2_18h_max");
		featureList.add("spo2_24h_max");
		featureList.add("spo2_6h_max");
		featureList.add("temperature_12h_max");
		featureList.add("temperature_18h_max");
		featureList.add("temperature_24h_max");
		featureList.add("temperature_6h_max");
		featureList.add("hr_12h_min");
		featureList.add("hr_18h_min");
		featureList.add("hr_24h_min");
		featureList.add("hr_6h_min");
		featureList.add("map_12h_min");
		featureList.add("map_18h_min");
		featureList.add("map_24h_min");
		featureList.add("map_6h_min");
		featureList.add("rr_12h_min");
		featureList.add("rr_18h_min");
		featureList.add("rr_24h_min");
		featureList.add("rr_6h_min");
		featureList.add("sbp_12h_min");
		featureList.add("sbp_18h_min");
		featureList.add("sbp_24h_min");
		featureList.add("sbp_6h_min");
		featureList.add("sspo2_12h_min");
		featureList.add("spo2_18h_min");
		featureList.add("spo2_24h_min");
		featureList.add("spo2_6h_min");
		featureList.add("temperature_12h_min");
		featureList.add("temperature_18h_min");
		featureList.add("temperature_24h_min");
		featureList.add("temperature_6h_min");
		featureList.add("hematocrit_min");
		featureList.add("hematocrit_max");
		featureList.add("wbc_min");
		featureList.add("wbc_max");
		featureList.add("glucose_min");
		featureList.add("glucose_max");
		featureList.add("bicarbonate_min");
		featureList.add("bicarbonate_max");
		featureList.add("potassium_min");
		featureList.add("potassium_max");
		featureList.add("sodium_min");
		featureList.add("sodium_max");
		featureList.add("bun_min");
		featureList.add("bun_max");
		featureList.add("ccreatinine_min");
		featureList.add("creatinine_max");
		featureList.add("age");
		featureList.add("vent");
		featureList.add("gcs");
		featureList.add("vasopressor");
		featureList.add("gender");
		featureList.add("icd9_code");

		return featureList;
	}

	public static ArrayList<String> getSelected(ArrayList<String> featureList) {
		featureList.add("bun_min");
		featureList.add("bun_max");
		featureList.add("age");
		featureList.add("bicarbonate_min");
		featureList.add("sodium_max");
		featureList.add("rr_6h_min");
		featureList.add("rr_12h_min");
		featureList.add("icd9_code");
		featureList.add("rr_18h_min");
		featureList.add("bicarbonate_max");
		featureList.add("rr_24h_min");
		featureList.add("gcs");
		featureList.add("sbp_24h_min");
		featureList.add("spo2_24h_min");
		featureList.add("temperature_6h_min");
		featureList.add("hr_24h_min");
		featureList.add("map_24h_min");
		featureList.add("temperature_6h_max");
		featureList.add("sbp_18h_min");
		featureList.add("temperature_12h_min");
		return featureList;
	}

	public static double timeStopper(int k, long startTime) {
		long eucStopTime = System.nanoTime();
		long eucDuration = eucStopTime - startTime;
//		final double eucMinutes = ((double) eucDuration * 0.0000000000166667);
//		System.out.println("k = " + k + " took " + eucMinutes + "min");
		return eucDuration;
	}
}