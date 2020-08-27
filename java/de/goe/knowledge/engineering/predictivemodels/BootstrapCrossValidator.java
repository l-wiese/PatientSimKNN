package de.goe.knowledge.engineering.predictivemodels;

import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class BootstrapCrossValidator extends CrossValidator {
	private boolean resampleExistingEntries;
	private double ratioAlive;
	private Instances[] data;
	private Instances oversampledData;
	public BootstrapCrossValidator(Instances instances, int numberOfPartitions,
									boolean resampleExistingEntries, double ratioAlive) {
		super(instances, numberOfPartitions, ratioAlive);
		this.resampleExistingEntries = resampleExistingEntries;
		this.ratioAlive = ratioAlive;
		oversampledData = null;
		data = null;
		name = "Bootstrap";
	}
	
	/**
	 * Advances the internal fold counter by one and returns the next fold.
	 * @return the next fold as an array of the form {training set, test set}.
	 */
	public Instances[] getNextFold() {
		data = super.getNextFold();
		
		
		BootstrapSampler sampler = new BootstrapSampler (data[0]);
		
		if (App.mode == App.modes.Bootstrap) {
			sampler.oversample(this.resampleExistingEntries, this.ratioAlive);
		}
		if (App.mode == App.modes.Undersample) {
			sampler.undersample(this.resampleExistingEntries, this.ratioAlive);
		}
		if (App.mode == App.modes.SMOTE) {
			sampler.smote();
		}
		
		oversampledData = sampler.getData();
		
		 // Construct the oversampled result set - Change second entry if both training and test sets are to be oversampled
		data = new Instances[]{oversampledData, data[1]}; 
		sampler = null;
		
		// Garbage collect the sampler, freeing all its instances to prevent memory leaks
		System.gc();

		return data;
	}
	
	/**
	 * Saves the current fold to a file
	 * @param trainingFile
	 * @param testFile
	 */
	public void saveFoldToArff(String trainingFile, String testFile) {
		InstancesHelper.saveToArff(oversampledData, trainingFile);
		InstancesHelper.saveToArff(data[1], testFile);
	}
}
