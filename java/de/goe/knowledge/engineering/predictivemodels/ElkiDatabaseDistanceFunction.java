package de.goe.knowledge.engineering.predictivemodels;

import java.util.HashMap;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.VectorTypeInformation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.AbstractNumberVectorDistanceFunction;

public class ElkiDatabaseDistanceFunction extends AbstractNumberVectorDistanceFunction {

	public static final ElkiDatabaseDistanceFunction STATIC = new ElkiDatabaseDistanceFunction();
	HashMap<Integer, Double> distances;
	int counter = 0;
	NumberVector currentPatient;
	double distanceResult;
	private DataFromDatabase dfdb;

	@Deprecated
	public ElkiDatabaseDistanceFunction() {
		// super(8);
		this.dfdb = new DataFromDatabase();
	}

	public double distance(NumberVector o1, NumberVector o2) {
		try {
			if (!o1.getValue(0).equals(currentPatient.getValue(0))) {
				distances = dfdb.getDistancesFrom("mimiciii.elkicosine", o1.getValue(0).intValue());
				currentPatient = o1;
			}
		} catch (NullPointerException e) {
			distances = dfdb.getDistancesFrom("mimiciii.elkicosine", o1.getValue(0).intValue());
			currentPatient = o1;
		}

		int key = o2.getValue(0).intValue();
		try {
			distanceResult = distances.get(key);
		} catch (NullPointerException e) {
			System.err
					.println("There is no Distance saved in database for " + o1.getValue(0) + " and " + o2.getValue(0));
			e.printStackTrace();
		}

		return distanceResult;
	}

	VectorTypeInformation<NumberVector> VARIABLE_LENGTH = VectorTypeInformation.typeRequest(NumberVector.class);

	@Override
	public String toString() {
		return "DatabaseDistanceFunction";
	}
}
