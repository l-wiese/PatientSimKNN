package de.goe.knowledge.engineering.predictivemodels;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.core.Instance;
import weka.core.Instances;

public class InstancesHelper {
	private final static Logger LOGGER = Logger.getLogger(BootstrapSampler.class.getName());
	
	
	/**
	 * Returns the number of instances labeled as alive in the given data set
	 * @param instances The data set
	 * @return Number of alive instances
	 */
	public static int getAliveCount (Instances instances) {
		int aliveCount = 0;
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.get(i);
			if (instance.value(instances.numAttributes()-1) == 0) {
				aliveCount++;
			}
		}
		return aliveCount;
	}
	
	
	/**
	 * Returns the number of instances labeled as dead in the given data set
	 * @param instances The data set
	 * @return Number of dead instances
	 */
	public static int getDeadCount (Instances instances) {
		int deadCount = 0;
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.get(i);
			if (instance.value(instances.numAttributes()-1) != 0) {
				deadCount++;
			}
		}
		return deadCount;
	}
	
	

	public static void saveToArff(Instances data, String outputFile) {
		
		// Save to output file
		try {
			Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));
			
			writer.write(data.toString());
			
			writer.flush();
			writer.close();
			
		}
		catch (FileNotFoundException e) {
			LOGGER.log(Level.WARNING, "The file " + outputFile + " to be written to could not be found");
		}
		catch (IOException e) {
			LOGGER.log(Level.WARNING, "Something went wron {g while saving the file " + outputFile);
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Convenience function for loading a data set from a file into an Instances instance.
	 * Handles the possible exceptions using logging.
	 * @param file The path to the file to load from
	 * @return The data set as instance of Instances
	 */
	public static Instances loadInstancesFromFile(String file) {
		try {
			// Initialize & read data
			System.out.println ("Reading file " + file + "...");
			FileReader reader = new FileReader(file);
			Instances data = new Instances(reader);
			data.setClassIndex(data.numAttributes() - 1);
			return data;
		}
		catch (FileNotFoundException e) {
			LOGGER.log (Level.SEVERE, "Could not find file " + file);
			System.exit(1);
			return null;
		}
		catch (IOException e) {
			LOGGER.log (Level.SEVERE, "Something went wrong when reading " + file);
			System.exit(1);
			return null;
		}
	}

}
