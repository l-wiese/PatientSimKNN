package de.goe.knowledge.engineering.predictivemodels;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import weka.core.Instance;
import weka.core.Instances;

public class CSVTransformer {

	/**
	 * Transforms a file containing a Weka-friendly data set to an ELKI-friendly CSV.
	 * @param isCSV Whether the input file is a CSV (or, otherwise, an ARFF) file.
	 * @param containsClass Whether the input file contains a class entry
	 * @param input Path to the input file
	 * @param output Path to the output file
	 * @param features Names of features to be used
	 * @throws IOException If there is an error during reading/writing
	 */
	public void CSVTransformerWeka2Elki(boolean isCSV, boolean containsClass, String input, String output, ArrayList<String> features) throws IOException {
		if (isCSV) {
			Reader in = new FileReader(input + ".csv");
			Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);
	
			String file = output + ".csv";
			Writer writer = null;
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
	
			for (CSVRecord record : records) {
				int j = 0;
				while (j != features.size() - 1) {
					String feature = features.get(j);
					writer.write(record.get(feature) + " ");
					j++;
				}
				if (containsClass) {
					if (record.get("death").contains("100")) {
						writer.write("ALIVE" + "\n");
					} else {
						writer.write("DEATH" + "\n");
					}
				}
				else {
					writer.write("\n");
				}
			}
			writer.close();
		}
		else {
			Reader in = new FileReader(input);
			String file = output;
			Writer writer = new BufferedWriter(new OutputStreamWriter (new FileOutputStream(file), "utf-8"));
			Instances data = new Instances(in);
			for (Instance instance: data) {
				int j = 0;
				while (j < instance.numAttributes()-1) {
					writer.write(instance.value(instance.attribute(j)) + (j != instance.numAttributes()-2 || containsClass?",":""));
					j++;
				}
				if (containsClass) {
					if (instance.value(instance.numAttributes()-1) == 1f) {
						writer.write("ALIVE");					
					}
					else {
						writer.write("DEAD");
					}
				}
				writer.write("\n");
			}
			writer.flush();
			writer.close();
		}
	}

}
