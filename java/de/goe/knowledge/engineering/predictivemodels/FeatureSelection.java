package de.goe.knowledge.engineering.predictivemodels;

import java.io.FileReader;

import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.ClassifierAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Instances;

public class FeatureSelection {

	public FeatureSelection() throws Exception {
		FileReader reader;
		reader = new FileReader("data/weka.csv");
		Instances data = new Instances(reader);
		data.setClassIndex(data.numAttributes() - 1);

		ASSearch search = new Ranker();
		ClassifierAttributeEval eval = new ClassifierAttributeEval();
		eval.buildEvaluator(data);
		eval.evaluateAttribute(75);

		AttributeSelection attsel = new AttributeSelection();
		attsel.setEvaluator(eval); // set evaluation method
		attsel.setSearch(search); // set search method
		data.remove(75);
		attsel.SelectAttributes(data);
		System.out.println(attsel.toResultsString());
	}
}
