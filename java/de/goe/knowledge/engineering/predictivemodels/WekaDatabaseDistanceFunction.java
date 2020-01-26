package de.goe.knowledge.engineering.predictivemodels;

import java.util.HashMap;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.NormalizableDistance;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;

public class WekaDatabaseDistanceFunction extends NormalizableDistance
		implements TechnicalInformationHandler, java.lang.Cloneable {

	private static final long serialVersionUID = 1L;
	HashMap<Integer, Double> distances;
	int counter = 0;
	Instance currentPatient;
	double distanceResult;
	private DataFromDatabase dfdb;

	public WekaDatabaseDistanceFunction() {
		super();
		this.dfdb = new DataFromDatabase();
	}

	public WekaDatabaseDistanceFunction(Instances data) {
		super(data);
		this.dfdb = new DataFromDatabase();
	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;

		result = new TechnicalInformation(Type.MISC);
		result.setValue(Field.AUTHOR, "Nicole Sarna");
		result.setValue(Field.TITLE, "Distance from Database");

		return result;
	}

	@Override
	public String globalInfo() {
		return "Implementing a distance function.\n\n"
				+ "The distances are already calculated an stored in MonetDB.\n\n For more information, see:\n\n"
				+ getTechnicalInformation().toString();
	}

	@Override
	public double distance(Instance o1, Instance o2) {
		try {
			if (!o1.toString(0).equals(currentPatient.toString(0))) {
				distances = dfdb.getDistancesFrom("mimiciii.elkicosine", o1.index(0));
				currentPatient = o1;
			}
		} catch (NullPointerException e) {
			distances = dfdb.getDistancesFrom("mimiciii.elkicosine", o1.index(0));
			currentPatient = o1;
		}

		int key = o2.index(0);
		try {
			distanceResult = distances.get(key);
		} catch (NullPointerException e) {
			System.err
					.println("There is no Distance saved in database for " + o1.toString(0) + " and " + o2.toString(0));
			e.printStackTrace();
		}
		System.out.println(distanceResult);
		return distanceResult;
	}

	@Override
	protected double updateDistance(double currDist, double diff) {
		double result;

		result = currDist;
		result += diff * diff;

		return result;
	}

	@Override
	public String getRevision() {
		return "Data from Database using table mimiciii.elkicosine";
	}

}
