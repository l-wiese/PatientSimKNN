package de.goe.knowledge.engineering.predictivemodels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.lmu.ifi.dbs.elki.algorithm.classification.KNNClassifier;
import de.lmu.ifi.dbs.elki.data.ClassLabel;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.SimpleClassLabel;
import de.lmu.ifi.dbs.elki.data.SimpleClassLabel.Factory;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.query.knn.KNNQuery;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.FileBasedDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.bundle.MultipleObjectsBundle;
import de.lmu.ifi.dbs.elki.datasource.filter.ObjectFilter;
import de.lmu.ifi.dbs.elki.datasource.filter.typeconversions.ClassLabelFilter;
import de.lmu.ifi.dbs.elki.datasource.parser.NumberVectorLabelParser;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.evaluation.classification.ConfusionMatrix;
import de.lmu.ifi.dbs.elki.evaluation.classification.holdout.AbstractHoldout;

public class ElkiKNN<O> {
	int k;
	DistanceFunction distance;
	String file;
	String testFile;
	String testFileClassless;
	String trainingFile;
	
	double[][] testData;
	double[][] trainingData;
	ArrayList<String> testLabels;

	int tp = 0;
	int tn = 0;
	int fp = 0;
	int fn = 0;

	public <O> ElkiKNN(int k, DistanceFunction distance, String trainingFile, String testFile, String testFileClassless) {
		this.k = k;
		this.distance = distance;
		this.trainingFile = trainingFile;
		this.testFile = testFile;
		this.testFileClassless = testFileClassless;
		this.trainingData = this.testData = new double[0][0];
	}

	/**
	 * Runs the kNN algorithm using ELKI.
	 * @return The result as 2x2 confusion matrix
	 * @see http://elki.dbs.ifi.lmu.de/browser/elki/elki-classification/src/main/java/de/lmu/ifi/dbs/elki/application/ClassifierHoldoutEvaluationTask.java
	 */
	public ConfusionMatrix run() {
		Factory classLabelFactory = new SimpleClassLabel.Factory();
		ClassLabelFilter f = new ClassLabelFilter(0, classLabelFactory);
		List<ObjectFilter> l = Arrays.asList(f);
		

		NumberVectorLabelParser<DoubleVector> parser = new NumberVectorLabelParser<DoubleVector>(DoubleVector.FACTORY);
		

		// Initialize db connections, dbs, training and test set, the classifier and the relations
		DatabaseConnection dbcTraining = null;
		DatabaseConnection dbcTest = null;
//		DatabaseConnection dbcTestClassless = null;

				try {
					dbcTraining = new FileBasedDatabaseConnection(l, parser, trainingFile);
					
				} catch (Exception e) {
					System.err.println("The program was aborted. \n " + trainingFile + "The file could not be found.");
				}
				try {
					dbcTest = new FileBasedDatabaseConnection(l, parser, testFile);
//					dbcTestClassless = new FileBasedDatabaseConnection(null, parser, testFileClassless);
					
				} catch (Exception e) {
					System.err.println("The program was aborted. \n " + testFile + "The file could not be found.");
				}
				
		
		Database dbTraining = new StaticArrayDatabase(dbcTraining, null);
		dbTraining.initialize();


		MultipleObjectsBundle test = dbcTest.loadData();
//		MultipleObjectsBundle testClassless = dbcTestClassless.loadData();
		
//		System.out.println (">> " + testClassless.dataLength());
		
		ArrayList<ClassLabel> labels = new ArrayList<ClassLabel>();
		labels.add(classLabelFactory.makeFromString("ALIVE"));
		labels.add(classLabelFactory.makeFromString("DEAD"));
		
		KNNClassifier<O> algorithm = new KNNClassifier<O>(distance, k);
		
		Relation<ClassLabel> lrel = dbTraining.getRelation(TypeUtil.CLASSLABEL);
		algorithm.buildClassifier(dbTraining, lrel);
		
		//  Classification
		// FIXME: this part is still a big hack, unfortunately!
		int[][] confusionMatrix = new int[2][2];
		
		int lcol = AbstractHoldout.findClassLabelColumn(test);
		int tcol = (lcol == 0) ? 1 : 0;
//		System.out.println ("lcol" + lcol);
//		System.out.println (test.data(0, tcol));
		for (int i = 0, f1 = test.dataLength(); i < f1; ++i) {
			@SuppressWarnings("unchecked")
			O obj = (O) test.data(i, tcol);
			ClassLabel truelabel = (ClassLabel) test.data(i, lcol);
			ClassLabel predlabel = algorithm.classify(obj);
			
//			System.out.println (predlabel);
			
			int pred = (predlabel == labels.get(0))?1:0;
			int real = (truelabel == labels.get(0))?1:0; 

			confusionMatrix[pred][real]++;
		}
		ConfusionMatrix m = new ConfusionMatrix(labels, confusionMatrix);
//		System.out.println(m.toString());
		

		return m;
	}
}
;