package de.goe.knowledge.engineering.predictivemodels;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.SpreadSubsample;

public class Undersampling {

	public Undersampling(String file) {
		FileReader reader;
		Writer writer = null;
		try {
			reader = new FileReader("data/" + file + ".arff");
			Instances data = new Instances(reader);
			data.setClassIndex(data.numAttributes() - 1);

			SpreadSubsample filters = new SpreadSubsample();
			filters.setInputFormat(data); // Instances instances;
			filters.setDistributionSpread(2); // How many to increase
			Instances subSamplingInstances = Filter.useFilter(data, filters);

			writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream("data/" + file + "_undersampledSMOTE.arff"), "utf-8"));

			for (Instance i : subSamplingInstances) {
				writer.write(i + "\n");
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
