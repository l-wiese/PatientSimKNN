package de.goe.knowledge.engineering.predictivemodels;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;

public class KMeans {

	public KMeans() {
		Reader reader;
		try {
			reader = new FileReader("data/wekaOnlyAlive.arff");
			Instances data = new Instances(reader);

			SimpleKMeans kMeans = new SimpleKMeans();
			kMeans.setNumClusters(4284);
			kMeans.buildClusterer(data);
			Instances inst = kMeans.getClusterCentroids();

			String file = "data/weka_undersampling_centroids.arff";
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));

			for (Instance i : inst) {
				writer.write(i + "\n");
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
