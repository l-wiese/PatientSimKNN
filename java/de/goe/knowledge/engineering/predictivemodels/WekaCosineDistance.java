package de.goe.knowledge.engineering.predictivemodels;

import java.util.Vector;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.NormalizableDistance;
import weka.core.neighboursearch.PerformanceStats;

/**
 * Implements the Cosine distance function in Weka.
 */
public class WekaCosineDistance extends NormalizableDistance {
	
	static final long serialVersionUID = 42L;
	
	Instances instances;
	String attributeIndices;
	boolean invertSelection;
	Vector<Integer>attributeIndicesVector;

	/**
	 * Sum of a double vector.
	 * This probably exists already, but looking it up was slower than writing it myself.
	 * @param v The vector to be summed
	 * @return The sum of all vector elements
	 */
	public static double Sum(double[] v) {
		double sum = 0;
		for (double x : v) {
			sum += x;
		}
		return sum;
	}
	
	/**
	 * Constructor for the distance.
	 * Sets the attribute indices to first-last since it seems that
	 * a lot of entries in the Weka kNN-calculation are initialized as
	 * having empty attribute indices.
	 */
	public WekaCosineDistance() {
//		super();
		setAttributeIndices("first-last");
	}

	/**
	 * The actual distance function.
	 *  d_cos(A, B) = 1 - (A*B)/(||A||*||B||)
	 *				= 1 - ((sum_i=1..n A_i * B_i) / (sqrt(sum_i=1..n (A_i)²) * sqrt(sum_i=1..n (B_i)²)) / 2 + 1)
	 *		where each A_i is an attribute of A
	 *		The 1 - [...] inversion is to transform similarity into distance.
	 *		The [...] / 2 + 1 at the end adjusts the result to be within [0, 1]
	 *		since cosine similarity produces results in [-1, 1]
	 */
	@Override
	public double distance(Instance first, Instance second, double cutOffValue, PerformanceStats stats) {
		// Convert for convenience (and to make it look more like the above formula)
		attributeIndicesVector = new Vector<Integer>();
		for (int i = 0; i < first.classIndex(); i++) attributeIndicesVector.add(i); // FIXME Hack
		if (attributeIndicesVector.size() == 0) {
			System.err.println ("Something weird happened");
			return 0;
		}
		
		double[] a = first.toDoubleArray();
		double[] b = second.toDoubleArray();
		double[] A = new double[attributeIndicesVector.size()];
		double[] B = new double[attributeIndicesVector.size()];

		
		for (int i = 0; i < attributeIndicesVector.size(); i++) {
			A[i] = a[attributeIndicesVector.get(i)];
			B[i] = b[attributeIndicesVector.get(i)];
		}
		
		// Actual calculation
		double num = 0, denA = 0, denB = 0;
		for (int i = 0; i < attributeIndicesVector.size(); i++) {
			num += A[i] * B[i];
			denA+= A[i] * A[i];
			denB+= B[i] * B[i];
		}
		double den = Math.sqrt(denA) * Math.sqrt(denB);
		double result = 1 - ((num/den) + 1) / 2;
		
		return result;
	}
	
	/**
	 * Calculates the distance using parallel streams (possibly faster)
	 * @param first
	 * @param second
	 * @param cutOffValue
	 * @param stats
	 * @return
	 */
	public double distanceParallel(Instance first, Instance second, double cutOffValue, PerformanceStats stats) {
		// Convert for convenience (and to make it look more like the above formula)
		if (attributeIndicesVector.size() == 0) {
			System.err.println ("Something weird happened");
			return 0;
		}
		
		double[] a = first.toDoubleArray();
		double[] b = second.toDoubleArray();
		double[] A = new double[attributeIndicesVector.size()];
		double[] B = new double[attributeIndicesVector.size()];

		
		for (int i = 0; i < attributeIndicesVector.size(); i++) {
			A[i] = a[attributeIndicesVector.get(i)];
			B[i] = b[attributeIndicesVector.get(i)];
		}
		
		// Actual calculation
		double num = 0, denA = 0, denB = 0;
		for (int i = 0; i < attributeIndicesVector.size(); i++) {
			num += A[i] * B[i];
			denA+= A[i] * A[i];
			denB+= B[i] * B[i];
		}
		double den = Math.sqrt(denA) * Math.sqrt(denB);
		double result = 1 - ((num/den) + 1) / 2;
		
		return result;
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