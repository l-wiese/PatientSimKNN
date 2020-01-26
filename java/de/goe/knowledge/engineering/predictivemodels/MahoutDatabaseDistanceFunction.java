package de.goe.knowledge.engineering.predictivemodels;

import static java.lang.Math.toIntExact;

import java.util.Collection;
import java.util.HashMap;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.RefreshHelper;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public final class MahoutDatabaseDistanceFunction implements UserSimilarity {

	HashMap<Integer, Double> distances;
	DataModel dataModel;
	DataFromDatabase dfdb;
	int counter = 0;
	long currentPatient;
	double distanceResult;

	public MahoutDatabaseDistanceFunction(DataModel dataModel) throws TasteException {
		this.dataModel = dataModel;
		dfdb = new DataFromDatabase();
	}

	// @Override
	// public double[] itemSimilarities(long itemID1, long[] itemID2s) throws
	// TasteException {
	// double[] distances = new double[itemID2s.length];
	//
	// int i = 0;
	// for (long itemID2 : itemID2s) {
	// distances[i] = itemSimilarity(itemID1, itemID2);
	// i++;
	// }
	// return distances;
	// }

	@Override
	public double userSimilarity(long userID1, long userID2) throws TasteException {
		if (userID1 != currentPatient) {
			distances = dfdb.getDistancesFrom("mimiciii.mahoutcosine", toIntExact(userID1));
			currentPatient = userID1;
		}

		String key = userID2 + "";
		try {
			distanceResult = distances.get(key);
		} catch (NullPointerException e) {
			distanceResult = -200;
		}

		return distanceResult;
	}

	@Override
	public void setPreferenceInferrer(PreferenceInferrer inferrer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		alreadyRefreshed = RefreshHelper.buildRefreshed(alreadyRefreshed);
		RefreshHelper.maybeRefresh(alreadyRefreshed, dataModel);
	}

}
