package de.goe.knowledge.engineering.predictivemodels;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;

import de.lmu.ifi.dbs.elki.distance.distancefunction.CosineDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.ManhattanDistanceFunction;
import de.lmu.ifi.dbs.elki.evaluation.classification.ConfusionMatrix;
import weka.core.EuclideanDistance;
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

	public static void main(String[] args) throws Exception {
		// Create Files
		// ArrayList<String> features = getFeatures(new ArrayList<String>());
		// DataFromDatabase db = new DataFromDatabase();
		// db.createDataModelCSV(features, "model_top20");
		// db.createElkiCSV(features, "elki_alive");
		// db.createFrameworkFile(getFeatures(new ArrayList<>()), "weka_TESCHT", 2);
		// System.out.println("DONE");

		// Undersampling o = new Undersampling("weka_alive_smote");
		// Undersampling o2 = new Undersampling("weka_alive_Top20_smote");
		// CSVTransformer csv = new CSVTransformer();
		// csv.CSVTransformerWeka2Elki("weka_alive_undersampling_undersampledSMOTE",
		// "elki_alive_undersampling_undersampledSMOTE", getFeatures(new
		// ArrayList<>()));
		// csv.CSVTransformerWeka2Elki("weka_undersampling_centroids",
		// "elki_undersampling.centroids",
		// getFeatures(new ArrayList<>()));

		// KNN ELKI
		int[] kVals = { 1, 3, 5, 9, 15, 25 };

		System.out.println("ELKI");
		String elkiFile = "data/elki_undersampling.centroids.csv";

		// Changes from getFeatures() to getSelected() if you use Top20 file
		// int numFeatures = getFeatures(new ArrayList<>()).size();
		// if (elkiFile.contains("Top20")) {
		// numFeatures = getSelected(new ArrayList<>()).size();
		// }

		for (int i = 0; i < kVals.length; i++) {
			// double[] f = new double[numFeatures];
			// int val = numFeatures - 70;
			// for (int j = 0; j < numFeatures; j++) {
			// if (j > kVals[i]) {
			// f[j] = 80;
			// } else {
			// f[j] = val--;
			// }
			// System.out.println(f[j]);
			// }

			// WeightedEuclideanDistanceFunction wg = new
			// WeightedEuclideanDistanceFunction(f);
			// long startTime = System.nanoTime();
			// ElkiKNN<Object> knnE = new ElkiKNN<Object>(kVals[i], wg, elkiFile);
			// ConfusionMatrix m = knnE.run();
			// timeStopper(kVals[i], startTime);
			//
			// Evaluation e = new Evaluation(m.truePositives(0), m.falseNegatives(0),
			// m.falsePositives(0),
			// m.trueNegatives(0));
			// e.accuracy();
			// e.precision();
			// e.recall();
			// e.specificity();

			// System.out.println("________________ OWN ________________ ");
			// long startTime = System.nanoTime();
			// ElkiKNN<Object> knnE = new ElkiKNN<Object>(kVals[i],
			// ElkiDatabaseDistanceFunction.STATIC, elkiFile);
			// ConfusionMatrix m = knnE.run();
			// timeStopper(kVals[i], startTime);
			//
			// Evaluation e = new Evaluation(m.truePositives(0), m.falseNegatives(0),
			// m.falsePositives(0),
			// m.trueNegatives(0));
			// e.accuracy();
			// e.precision();
			// e.recall();
			// e.specificity();

			System.out.println("________________ EUCLIDEAN ________________ ");
			long startTime = System.nanoTime();
			ElkiKNN<Object> knnE = new ElkiKNN<Object>(kVals[i], EuclideanDistanceFunction.STATIC, elkiFile);
			ConfusionMatrix m = knnE.run();
			timeStopper(kVals[i], startTime);

			Evaluation e = new Evaluation(m.truePositives(0), m.falseNegatives(0), m.falsePositives(0),
					m.trueNegatives(0));
			e.accuracy();
			e.precision();
			e.recall();
			e.specificity();

			System.out.println("________________ MANHATTAN ________________ ");
			long startTime2 = System.nanoTime();
			ElkiKNN<Object> knnE2 = new ElkiKNN<Object>(kVals[i], ManhattanDistanceFunction.STATIC, elkiFile);
			ConfusionMatrix m2 = knnE2.run();
			timeStopper(kVals[i], startTime2);

			Evaluation e2 = new Evaluation(m2.truePositives(0), m2.falseNegatives(0), m2.falsePositives(0),
					m2.trueNegatives(0));
			e2.accuracy();
			e2.precision();
			e2.recall();
			e2.specificity();

			System.out.println("________________ COSINE ________________ ");
			long startTime3 = System.nanoTime();
			ElkiKNN<Object> knnE3 = new ElkiKNN<Object>(kVals[i], CosineDistanceFunction.STATIC, elkiFile);
			ConfusionMatrix m3 = knnE3.run();
			timeStopper(kVals[i], startTime3);

			Evaluation e3 = new Evaluation(m3.truePositives(0), m3.falseNegatives(0), m3.falsePositives(0),
					m3.trueNegatives(0));
			e3.accuracy();
			e3.precision();
			e3.recall();
			e3.specificity();
			System.out.println("---------------------------------------------------------");
		}

		System.out.println("---------------------------------------------------------");
		// KNN MAHOUT
		System.out.println("MAHOUT");
		String path2CSVFile = "data/model.csv";
		DataFromDatabase dfdb = new DataFromDatabase();
		HashMap<Integer, String> patientMortalityMap = new HashMap<>();
		dfdb.setPatientMortalityMap(patientMortalityMap);

		try {
			for (int i = 0; i < kVals.length; i++) {
				long startTime = System.nanoTime();
				DataModel model = new FileDataModel(new File(path2CSVFile));
				DataFromDatabase bfdb = new DataFromDatabase();

				// Parameters to change
				MahoutKNN knnM = new MahoutKNN(kVals[i], new EuclideanDistanceSimilarity(model), model, bfdb);

				org.apache.mahout.classifier.ConfusionMatrix mm = knnM.run();
				timeStopper(kVals[i], startTime);

				int[][] matrix = mm.getConfusionMatrix();

				Evaluation e = new Evaluation(matrix[0][0], matrix[0][1], matrix[1][0], matrix[1][1]);
				e.accuracy();
				e.precision();
				e.recall();
				System.out.println("---------------------------------------------------------");
			}
		} catch (IOException e) {
			System.err.println("The program was aborted. \n " + "The file " + path2CSVFile + " could not be found.");
		}

		// KNN WEKA
		System.out.println("WEKA");

		String file = "weka_alive_undersampling.arff";

		for (int i = 0; i < kVals.length; i++) {
			System.out.println("________________ EUCLIDEAN ________________ ");
			long startTime = System.nanoTime();
			FileReader reader = new FileReader("data/" + file);

			Instances data = new Instances(reader);
			// last column is the classifier
			data.setClassIndex(data.numAttributes() - 1);
			WekaKNN knnW = new WekaKNN(kVals[i], new EuclideanDistance(), file);

			double[][] matrix = knnW.run();
			timeStopper(kVals[i], startTime);

			Evaluation e = new Evaluation(matrix[1][1], matrix[1][0], matrix[0][1], matrix[0][0]);
			e.accuracy();
			e.precision();
			e.recall();
			e.specificity();

			System.out.println("________________ MANHATTAN ________________ ");
			long startTime1 = System.nanoTime();
			WekaKNN knnW1 = new WekaKNN(kVals[i], new weka.core.ManhattanDistance(), file);

			double[][] matrix1 = knnW1.run();
			timeStopper(kVals[i], startTime1);

			Evaluation e1 = new Evaluation(matrix1[1][1], matrix1[1][0], matrix1[0][1], matrix1[0][0]);
			e1.accuracy();
			e1.precision();
			e1.recall();
			e1.specificity();
			System.out.println("---------------------------------------------------------");
		}
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

	public static void timeStopper(int k, long startTime) {
		long eucStopTime = System.nanoTime();
		long eucDuration = eucStopTime - startTime;
		final double eucMinutes = ((double) eucDuration * 0.0000000000166667);
		System.out.println("k = " + k + " took " + eucMinutes + "min");
	}
}