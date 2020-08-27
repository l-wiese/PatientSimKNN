package de.goe.knowledge.engineering.predictivemodels;

import java.util.Vector;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.NormalizableDistance;
import weka.core.neighboursearch.PerformanceStats;


/**
 * Weka "distance function" which simply returns 1 for all cases, for speed performance reference.
 * @see de.goe.knowledge.engineering.predictivemodels.
 */
public class WekaFixedDistance extends NormalizableDistance {
	
	static final long serialVersionUID = 43L;
	
	Instances instances;
	String attributeIndices;
	boolean invertSelection;
	Vector<Integer>attributeIndicesVector;
	/**
	 * The "distance function"
	 * @param first Anything (can be set to null)
	 * @param second Anything(can be set to null)
	 * @param cutOffValue Anything (can be set to 0f)
	 * @param stats Anything (can be set to null)
	 */
	@Override
	public double distance(Instance first, Instance second, double cutOffValue, PerformanceStats stats) {
		return 1f;
	}


	@Override
	public double distance(Instance first, Instance second, double cutOffValue) {
		return distance(first, second);
	}

	@Override
	public double distance(Instance first, Instance second) {
		return distance(first, second, 0, null);
	}

	@Override
	public void postProcessDistances(double[] distances) { }

	@Override
	public void update(Instance ins) {	}

	@Override
	public void clean() { }

	@Override
	public String getRevision() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String globalInfo() {
		// TODO Auto-generated method stub
		return "global info";
	}

	@Override
	protected double updateDistance(double currDist, double diff) {
		currDist += diff;
		return currDist;
	}	
}