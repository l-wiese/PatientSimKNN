package de.goe.knowledge.engineering.predictivemodels;

import java.util.Random;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;

/**
 * Sampler which uses Bootstrap Sampling (Efron, 1979) to over- or undersample the data set
 * and creates a .arff-file containing the results.
 * This class has been rebuilt from the previous OversamplingBootstrap class
 * to integrate OOP-patterns (since this is Java) and to divide the functions (instead of having one big function)
 *
 */
public class BootstrapSampler {
	/**
	 * Reads an .arff-file and creates an oversampled .arff-file with the given ratio of alive to dead instances.
	 * Output will be saved to file_bootstrap.arff.
	 * @param file The input file
	 * @param ratioAlive The fraction of the dead instances in relation to alive instances. Use 1 for balanced classes.
	 * @param resampleExistingEntries Whether the existing entries in the minority class should be reused or tossed
	 * 			in favor of resampled entries.
	 */
		
	Instances loadedData; // The data set loaded from the file
	Instances data; // The "actual" data set which is modified through over-/undersampling
	int aliveCount;
	int deadCount;
	String file;
	
	/**
	 * Initializes the sampler by loading a data set from a .arff file.
	 * @param file The path of a .arff file containing the data set.
	 */
	public BootstrapSampler (String file) {
		String loadedFile = file + ".arff";
		this.file = file;
		this.loadedData = InstancesHelper.loadInstancesFromFile(loadedFile);
		resetCounts();
	}
	
	/**
	 * Initializes the sampler using a data set (instance of Instances).
	 * @param instances The data set
	 */
	public BootstrapSampler (Instances instances) {
		this.loadedData = instances;
		this.file = null;
		this.data = new Instances(instances);
		resetCounts();
	}
	

	/**
	 * Rereads the number of alive/deceased patients from original data set
	 * and saves them to bootstrap.aliveCount / bootstrap.deadCount
	 */
	public void resetCounts() {
		this.aliveCount = InstancesHelper.getAliveCount(loadedData);
		this.deadCount = this.loadedData.numInstances() - aliveCount;
	}
	
	/**
	 * Applies smote oversampling on the loaded data set.
	 */
	public void smote() {
		Instances newData = new Instances(loadedData);
		double percentage = (double)InstancesHelper.getAliveCount(newData) / (double)InstancesHelper.getDeadCount(newData) * 100d - 100;
//		System.out.println (InstancesHelper.getAliveCount(newData) + " -- " + InstancesHelper.getDeadCount(newData));
//		System.out.println (percentage);
		SMOTE filters = new SMOTE();
		try {
			filters.setInputFormat(newData); // Instances instances;
			filters.setPercentage(percentage); // How many to increase
			filters.setRandomSeed(App.randomSeed);
			newData = Filter.useFilter(newData, filters);
			this.data = newData;
//			System.out.println("SMOTE: " + InstancesHelper.getAliveCount(newData) + " to " + InstancesHelper.getDeadCount(newData));
		} 
		catch (Exception e) {
			System.err.println ("Error applying SMOTE:");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Performs randomized undersampling on the loaded data set.
	 * @param resampleExistingEntries Whether to reuse the existing entries in the final set or construct an all-new set
	 * @param ratioAlive goal ratio of alive-to-dead people in the resulting data set.
	 */
	public void undersample(boolean resampleExistingEntries, double ratioAlive) {
		Instances newData = new Instances (loadedData);
		int n = loadedData.size(); // the number of draws
		int alivesLeftToRemove = InstancesHelper.getAliveCount (newData) - InstancesHelper.getDeadCount(newData);
//		System.out.println (alivesLeftToRemove + "/" + loadedData.size());
		Random random = new Random(App.randomSeed);
		int drawnSample = random.nextInt(n);
		
		while (alivesLeftToRemove > 0) {
			drawnSample = Math.abs(random.nextInt() % newData.size());
			Instance instance = newData.get(drawnSample);
			if (instance.value(loadedData.numAttributes()-1) == 0) {
				alivesLeftToRemove--;
				newData.remove(drawnSample);
			}
		}
//		System.out.println ("orig " + InstancesHelper.getAliveCount(loadedData) + " to " + InstancesHelper.getDeadCount(loadedData));
//		System.out.println ("UNDERSAMPLED " + InstancesHelper.getAliveCount(newData) + " to " + InstancesHelper.getDeadCount(newData));
		
		this.data = newData;
	}
	
	/**
	 * Performs oversampling on the data set stored in this.loadedData and saves the result to this.data 
	 * @param resampleExistingEntries Whether to reuse the existing entries in the final set or construct an all-new set
	 * @param ratioAlive goal ratio of alive-to-dead people in the resulting data set.
	 */
	public void oversample(boolean resampleExistingEntries, double ratioAlive) {
		int sampleSize = loadedData.size();				
		Instances newData = drawBootstrapSample (resampleExistingEntries, loadedData, sampleSize, ratioAlive);
		this.data = newData;
	}
	
	
	/**
	 * Draws a bootstrap sample from this sampler's data set.
	 * @param loadedData The data set to sample from (usually loaded from a file)
	 * @param sampleSize Number of samples to be drawn
	 * @return The drawn samples
	 */
	public static Instances drawBootstrapSample(boolean resampleExistingEntries, Instances loadedData, int sampleSize, double ratioAlive) {
		Random random = new Random(App.randomSeed);
		Instances newData = new Instances(loadedData); 
		int n = sampleSize; // the number of draws
		int alivesLeftToDraw = (int)(n/2 * ratioAlive);
		int deadLeftToDraw = sampleSize - (int)(n/2 * ratioAlive);
		int drawnSample = random.nextInt(n);

		if (resampleExistingEntries) { newData.clear() ;};
		newData.clear();
		while (alivesLeftToDraw > 0 || deadLeftToDraw > 0) {
			drawnSample = Math.floorMod(random.nextInt(), n);
			Instance instance = loadedData.get(drawnSample);
			if (instance.value(loadedData.numAttributes()-1) == 0 && alivesLeftToDraw > 0) {
				alivesLeftToDraw--;
				newData.add(instance);
			}
			if (instance.value(loadedData.numAttributes()-1) != 0 && deadLeftToDraw > 0) {
				deadLeftToDraw--;
				newData.add(instance);
			}	
		}
		return newData;
	}
	
	/**
	 * Returns the (possibly resampled) data set within the sampler
	 * @return The data set
	 */
	public Instances getData() {
		return data;
	}
	
	
	/**
	 * Saves the data set within the sampler to a file
	 * @param file Path to the file
	 */
	public void saveToArff(String file) {
		InstancesHelper.saveToArff(data, file);
	}
}
 