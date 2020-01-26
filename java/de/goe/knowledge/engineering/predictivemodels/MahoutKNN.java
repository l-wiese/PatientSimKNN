package de.goe.knowledge.engineering.predictivemodels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.classifier.ConfusionMatrix;

public class MahoutKNN {

	int k;
	String prediction = "0";
	UserSimilarity userSimilarity;
	DataModel dm;
	DataFromDatabase dbf;
	HashMap<Integer, String> mortalityMap;
	String correctLabel;

	public MahoutKNN(int k, UserSimilarity userSimilarity, DataModel dm, DataFromDatabase dbf) {
		this.k = k;
		this.userSimilarity = userSimilarity;
		this.dm = dm;
		this.dbf = dbf;

		dbf.setPatientMortalityMap(mortalityMap);
		this.mortalityMap = dbf.getPatientMortalityMap();
	}

	public ConfusionMatrix run() {
		List<String> labels = new ArrayList<String>();
		labels.add("100");
		labels.add("0");
		ConfusionMatrix confusionMatrix = new ConfusionMatrix(labels, "UNKOWN");

		try {
			LongPrimitiveIterator it = dm.getUserIDs();
			while (it.hasNext()) {
				Long patient = it.next();
				int key = Integer.parseInt(patient + "");
				confusionMatrix.addInstance(mortalityMap.get(key), predictLabel(patient));
			}
		} catch (TasteException e) {
			e.printStackTrace();
		}
		System.out.println(confusionMatrix);
		return confusionMatrix;
	}

	public String predictLabel(long patientID) {
		int deadCounter = 0;
		int aliveCounter = 0;

		try {
			UserNeighborhood nn = new NearestNUserNeighborhood(k, userSimilarity, dm);
			long[] neighbors = nn.getUserNeighborhood(patientID);

			for (long patient : neighbors) {
				int key = Integer.parseInt(patient + "");
				if (!mortalityMap.get(key).equals("0")) {
					deadCounter++;
				} else {
					aliveCounter++;
				}
			}
		} catch (TasteException e) {
			e.printStackTrace();
		}

		if (aliveCounter <= deadCounter) {
			prediction = "100";
		}
		return prediction;
	}
}
