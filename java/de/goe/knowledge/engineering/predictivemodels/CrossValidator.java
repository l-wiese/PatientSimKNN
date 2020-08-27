package de.goe.knowledge.engineering.predictivemodels;

import java.util.ArrayList;

import weka.core.Instance;
import weka.core.Instances;

public class CrossValidator {
	private Instances instances;
	private ArrayList<Instances> partitions;
	private int numberOfPartitions;
	private int partitionSize;
	private int currentFold;
	private int numberOfFolds;
	private ArrayList<ArrayList<String>> currentFoldLabels;
	public String name = "Generic CV";
	
	public CrossValidator (Instances instances, int numberOfPartitions, double ratioAlive) {
		this.instances = new Instances (instances); // Create a copy of instances instead of just taking the reference
		this.numberOfPartitions = numberOfPartitions;
		partitions = new ArrayList<Instances>();
		partitionSize = (int)(instances.size() / numberOfPartitions);
		currentFold = 0;
		currentFoldLabels = new ArrayList<ArrayList<String>>();
		currentFoldLabels.add(new ArrayList<String>());
		currentFoldLabels.add(new ArrayList<String>());
		numberOfFolds = numberOfPartitions;
	}
	
	
	/**
	 * Partitions the data set into numberOfPartitions partitions.
	 * The partitions are stratified (i.e. the ratio of alive-to-dead patients should be
	 *  equal to that of the overall data set, given in aliveRatio)
	 */
	public void partition() {
		partitions.clear();
		
		// Get lists of dead/alive instances
		Instances aliveInstances = new Instances (instances);
		Instances deadInstances = new Instances (instances);
		aliveInstances.clear();
		deadInstances.clear();
		for (int i = 0; i < instances.size(); i++) {
			if (instances.get(i).value(instances.numAttributes() - 1) == 0) {
				aliveInstances.add(instances.get(i));
			}
			else {
				deadInstances.add(instances.get(i));
			}
		}
		
		InstancesHelper.getAliveCount(instances);
		int numAlivePerPartition = aliveInstances.size() / numberOfPartitions;
		// Construct partitions
		for (int i = 0; i < numberOfPartitions; i++) {
			Instances currentPartition = new Instances (instances);
			currentPartition.clear();
			// Add entries to partition in the right ratio
			for (int j = 0; j < partitionSize; j++) {
				if (j < numAlivePerPartition) {
					currentPartition.add(aliveInstances.get(0));
					aliveInstances.remove(0);
				}
				else {
					if (deadInstances.size() <= 0) break;
					currentPartition.add(deadInstances.get(0));
					deadInstances.remove(0);
				}
			} 
			partitions.add(currentPartition);
		}
		// Add all "leftover" entries to the last partition
		partitions.get(partitions.size()-1).addAll(aliveInstances);
		partitions.get(partitions.size()-1).addAll(deadInstances);
	}
	
	/**
	 * Returns whether there is another fold or not
	 * @return whether there is another fold or not
	 */
	public boolean hasNext() {
		return currentFold < numberOfFolds;
	}
	
	/**
	 * Resets the CV (i.e. start with the first fold again)
	 */
	public void reset() {
		currentFold = 0;
	}
	
	/**
	 * Returns the next training and test data sets
	 * @return an array where [0] is the training and [1] the test data set
	 */
	public Instances[] getThisFold() {
		Instances trainingInstances = new Instances(instances);
		Instances testInstances = new Instances(instances);
		currentFoldLabels.get(0).clear();
		currentFoldLabels.get(1).clear();
		trainingInstances.clear();   // Since there's no empty constructor for Instances
		testInstances.clear();
		
		// Loop through all partition subsets.
		// Add the Add the subset to the training set otherwise.
		for (int i = 0; i < partitions.size(); i++) {
			if (i == currentFold) {
				testInstances = partitions.get(i);
				for (Instance instance : partitions.get(i)) {
					String label = Double.toString(instance.classValue());
					currentFoldLabels.get(1).add(label);
				}
			}
			else {
				trainingInstances.addAll(partitions.get(i));
				for (Instance instance : partitions.get(i)) {
					String label = Double.toString(instance.classValue());
					currentFoldLabels.get(0).add(label);
				}
			}		
		}
//		System.out.println("");
		
		return new Instances[]{trainingInstances, testInstances};
	}
	
	public Instances[] getNextFold() {
		Instances[] r = getThisFold();
		currentFold++;
		return r;
	}
	
	/**
	 * Returns the current folds' training labels as string array
	 * @return Array of labels
	 */
	public ArrayList<String> getFoldTrainingLabels() {
		return currentFoldLabels.get(0);
	}
	
	/**
	 * Returns the current folds' test labels as string array
	 * @return Array of labels
	 */
	public ArrayList<String> getFoldTestLabels() {
		return currentFoldLabels.get(1);
	}
	
	/**
	 * Returns the ID of the current fold
	 * (not the whole fold - see getThisFold() instead)
	 * @return The ID of the current fold
	 */
	public int getCurrentFold() {
		return currentFold;
	}
	
	/** 
	 * Returns the name of the cross validator
	 * @return The name of the cross validator
	 */
	public String getCVName() {
		return name;
	}
	
	
	/**
	 * Returns a standardized file name for the current fold's training set
	 * @return the file name
	 */
	public String getCurrentFoldTrainingFilePath() {
		return getCVName() + "-" + getCurrentFold() + "-Training";
	}
	
	/**
	 * Returns a standardized file name for the current fold's test set
	 * @return the file name
	 */
	public String getCurrentFoldTestFilePath() {
		return getCVName() + "-" + getCurrentFold() + "-Test";
	}
}
