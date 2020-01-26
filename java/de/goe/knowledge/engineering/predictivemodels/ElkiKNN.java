package de.goe.knowledge.engineering.predictivemodels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.lmu.ifi.dbs.elki.algorithm.classification.KNNClassifier;
import de.lmu.ifi.dbs.elki.data.ClassLabel;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.SimpleClassLabel;
import de.lmu.ifi.dbs.elki.data.SimpleClassLabel.Factory;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.FileBasedDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.MultipleObjectsBundleDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.bundle.MultipleObjectsBundle;
import de.lmu.ifi.dbs.elki.datasource.filter.ObjectFilter;
import de.lmu.ifi.dbs.elki.datasource.filter.typeconversions.ClassLabelFilter;
import de.lmu.ifi.dbs.elki.datasource.parser.NumberVectorLabelParser;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.evaluation.classification.ConfusionMatrix;
import de.lmu.ifi.dbs.elki.evaluation.classification.holdout.AbstractHoldout;
import de.lmu.ifi.dbs.elki.evaluation.classification.holdout.StratifiedCrossValidation;
import de.lmu.ifi.dbs.elki.evaluation.classification.holdout.TrainingAndTestSet;

public class ElkiKNN<O> {
	int k;
	DistanceFunction distance;
	String file;

	int tp = 0;
	int tn = 0;
	int fp = 0;
	int fn = 0;

	public <O> ElkiKNN(int k, DistanceFunction distance, String file) {
		this.k = k;
		this.distance = distance;
		this.file = file;
	}

	/**
	 * @see http://elki.dbs.ifi.lmu.de/browser/elki/elki-classification/src/main/java/de/lmu/ifi/dbs/elki/application/ClassifierHoldoutEvaluationTask.java
	 */
	public ConfusionMatrix run() {
		StratifiedCrossValidation holdout = new StratifiedCrossValidation(10);

		Factory classLabelFactory = new SimpleClassLabel.Factory();
		ClassLabelFilter f = new ClassLabelFilter(0, classLabelFactory);
		List<ObjectFilter> l = Arrays.asList(f);

		NumberVectorLabelParser<DoubleVector> parser = new NumberVectorLabelParser<DoubleVector>(DoubleVector.FACTORY);

		String path2CSVFile = file;
		DatabaseConnection dbc = null;

		try {
			dbc = new FileBasedDatabaseConnection(l, parser, path2CSVFile);
		} catch (Exception e) {
			System.err.println("The program was aborted. \n " + "The file " + path2CSVFile + " could not be found.");
		}

		MultipleObjectsBundle allData = dbc.loadData();
		holdout.initialize(allData);

		KNNClassifier<O> algorithm = new KNNClassifier<O>(distance, k);

		ArrayList<ClassLabel> labels = holdout.getLabels();
		int[][] confusionMatrix = new int[2][2];

		for (int p = 0; p < holdout.numberOfPartitions(); p++) {
			TrainingAndTestSet partition = holdout.nextPartitioning();

			// Load the data set into a database structure (for indexing)
			Database db = new StaticArrayDatabase(new MultipleObjectsBundleDatabaseConnection(partition.getTraining()),
					null);
			db.initialize();

			// Train the classifier
			Relation<ClassLabel> lrel = db.getRelation(TypeUtil.CLASSLABEL);
			algorithm.buildClassifier(db, lrel);

			// FIXME: this part is still a big hack, unfortunately!
			MultipleObjectsBundle test = partition.getTest();
			int lcol = AbstractHoldout.findClassLabelColumn(test);
			int tcol = (lcol == 0) ? 1 : 0;

			for (int i = 0, f1 = test.dataLength(); i < f1; ++i) {
				@SuppressWarnings("unchecked")
				O obj = (O) test.data(i, tcol);
				ClassLabel truelabel = (ClassLabel) test.data(i, lcol);
				ClassLabel predlabel = algorithm.classify(obj);

				int pred = Collections.binarySearch(labels, predlabel);
				int real = Collections.binarySearch(labels, truelabel);

				confusionMatrix[pred][real]++;
			}
		}
		ConfusionMatrix m = new ConfusionMatrix(labels, confusionMatrix);
		System.out.println(m.toString());

		return m;
	}

	// public void runSingle() {
	// LeaveOneOut holdout = new LeaveOneOut();
	//
	// Factory classLabelFactory = new SimpleClassLabel.Factory();
	// ClassLabelFilter f = new ClassLabelFilter(0, classLabelFactory);
	//
	// List<ObjectFilter> l = Arrays.asList(f);
	// NumberVectorLabelParser<DoubleVector> parser = new
	// NumberVectorLabelParser<DoubleVector>(DoubleVector.FACTORY);
	// DatabaseConnection dbc = new FileBasedDatabaseConnection(l, parser,
	// "data/elki.csv");
	// MultipleObjectsBundle allData = dbc.loadData();
	// holdout.initialize(allData);
	//
	// KNNClassifier<O> algorithm = new KNNClassifier<O>(distance, k);
	// Collection<IndexFactory<?, ?>> indexFactories = null;
	//
	// ArrayList<ClassLabel> labels = holdout.getLabels();
	// int[][] confusion = new int[labels.size()][labels.size()];
	// // for (int p = 0; p < holdout.numberOfPartitions(); p++) {
	// TrainingAndTestSet partition = holdout.nextPartitioning();
	//
	// // Load the data set into a database structure (for indexing)
	// Database db = new StaticArrayDatabase(new
	// MultipleObjectsBundleDatabaseConnection(partition.getTraining()),
	// indexFactories);
	// db.initialize();
	//
	// // Train the classifier
	// Relation<ClassLabel> lrel = db.getRelation(TypeUtil.CLASSLABEL);
	// algorithm.buildClassifier(db, lrel);
	//
	// // FIXME: this part is still a big hack, unfortunately!
	// MultipleObjectsBundle test = partition.getTest();
	// int lcol = AbstractHoldout.findClassLabelColumn(test);
	// int tcol = (lcol == 0) ? 1 : 0;
	//
	// for (int i = 0, f1 = test.dataLength(); i < f1; ++i) {
	// long startTime = System.nanoTime();
	//
	// @SuppressWarnings("unchecked")
	// O obj = (O) test.data(i, tcol);
	// ClassLabel truelbl = (ClassLabel) test.data(i, lcol);
	// ClassLabel predlbl = algorithm.classify(obj);
	//
	// // System.out.println(test.data(i, 0) + " predicted label " + predlbl + " and
	// is
	// // " + truelbl);
	// // long eucStopTime = System.nanoTime();
	// // long eucDuration = eucStopTime - startTime;
	// // System.out.println("This took " + eucDuration + " nanosec");
	//
	// int pred = Collections.binarySearch(labels, predlbl);
	// int real = Collections.binarySearch(labels, truelbl);
	//
	// confusion[pred][real]++;
	// }
	// // ConfusionMatrix m = new ConfusionMatrix(labels, confusion);
	// }
}
