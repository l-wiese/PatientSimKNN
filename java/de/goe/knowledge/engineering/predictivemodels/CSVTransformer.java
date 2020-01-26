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

public class CSVTransformer {

	public void CSVTransformerWeka2Elki(String input, String output, ArrayList<String> features) throws IOException {
		Reader in = new FileReader("data/" + input + ".csv");
		Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);

		String file = "data/" + output + ".csv";
		Writer writer = null;
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));

		for (CSVRecord record : records) {
			int j = 0;
			while (j != features.size() - 1) {
				String feature = features.get(j);
				writer.write(record.get(feature) + " ");
				j++;
			}
			if (record.get("death").contains("100")) {
				writer.write("ALIVE" + "\n");
			} else {
				writer.write("DEAD" + "\n");
			}
		}
		writer.close();
	}

}
