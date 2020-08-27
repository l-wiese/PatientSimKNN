package de.goe.knowledge.engineering.predictivemodels;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


/**
 * Class which stores the results of evaluations and makes displaying/comparing them easier.
 *
 */

public class Analyzer {
	
	private Vector<kNNEvaluation> evaluations;
	public int evalN;
	
	
	/**
	 * Constructs an Analyzer which stores a list of evaluations
	 *  and allows specialized management and information functions on said list.
	 */
	public Analyzer() {	
		evaluations = new Vector<kNNEvaluation>();
		evalN = 0;
	}
	
	/**
	 * Adds an evaluation to the list of evaluations
	 * @param evaluation
	 */
	public void addEvaluation(kNNEvaluation evaluation) {
		evaluations.add(evaluation);
	}
	
	/**
	 * Removes the given evaluation from the list of evaluations
	 * @param evaluation
	 */
	public void removeEvaluation(Evaluation evaluation) {
		evaluations.remove(evaluation);
	}
	
	/**
	 * Returns the list of evaluations
	 * @return The list of evaluations
	 */
	public Vector<kNNEvaluation> getEvaluations() {
		return evaluations;
	}
	
	/**
	 * Clears the list of evaluations
	 */
	public void clearEvaluations() {
		evaluations.clear();
	}
	
	/**
	 * Returns the number of evaluations with a specific name within the analyzer.
	 * @param name The name of the evaluations to be counted
	 * @return The number of evaluations with the given name
	 */
	public long getNumberOfEvaluationsByName(String name) {
		long n = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).count();
		return n;
	}
	
	/**
	 * Returns an evaluation containing the average values of all evaluations with the given name
	 * @param name The name of the evaluations to be considered
	 * @return An evaluation containing the average values of all evaluations with the given name
	 */
	public Evaluation getAverages(String name) {
                double averageTP = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> evaluation.TP).average().orElse(Double.NaN);
                double averageFN = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> evaluation.FN).average().orElse(Double.NaN);
                double averageFP = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> evaluation.FP).average().orElse(Double.NaN);
                double averageTN = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> evaluation.TN).average().orElse(Double.NaN);
                return new Evaluation(name, averageTP, averageFN, averageFP, averageTN);           
	}
	
	/**
	 * Returns an evaluation containing the variance values of all evaluations with the given name
	 * @param name The name of the evaluations to be considered
	 * @return An evaluation containing the variance values of all evaluations with the given name
	 */
	public Evaluation getVariances(String name) {
                Evaluation avg = getAverages(name);
                double varianceTP = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> Math.pow(evaluation.TP - avg.TP, 2)).average().orElse(Double.NaN);
                double varianceFN = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> Math.pow(evaluation.FN - avg.FN, 2)).average().orElse(Double.NaN);
                double varianceFP = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> Math.pow(evaluation.FP - avg.FP, 2)).average().orElse(Double.NaN);
                double varianceTN = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> Math.pow(evaluation.TN - avg.TN, 2)).average().orElse(Double.NaN);
                return new Evaluation("Averages", varianceTP, varianceFN, varianceFP, varianceTN);           
	}
	
	
	/** FIXME This is not a pretty solution
	 * Returns an evaluation containing the average statistical values of all evaluations with the given name
	 * @param name The name of the evaluations to be considered
	 * @param extraEvaluation an evaluation which is temporarily added to the evaluations list. For comparison purposes.
	 * @return A Vector containing the average values of all evaluations with the given name in the order accuracy, precision, recall, specificity.
	 */
	public Vector<Double> getAverageValues(String name, kNNEvaluation extraEvaluation) {
				if (extraEvaluation != null) {
					evaluations.add(extraEvaluation); // FIXME This is super ugly
				}
                double averageAcc = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> evaluation.accuracy()).average().orElse(Double.NaN);
                double averagePre = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> evaluation.precision()).average().orElse(Double.NaN);
                double averageRec = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> evaluation.recall()).average().orElse(Double.NaN);
                double averageSpe = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> evaluation.specificity()).average().orElse(Double.NaN);
                double averageTime= evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> evaluation.time).average().orElse(Double.NaN);
                evaluations.remove(extraEvaluation); // FIXME This too
                
                Vector<Double> result = new Vector<Double>();
                result.add(averageAcc);
                result.add(averagePre);
                result.add(averageRec);
                result.add(averageSpe);
                result.add(averageTime);
                return result;
	}
	
	/** 
	 * Returns an evaluation containing the average statistical values of all evaluations with the given name
	 * @param name The name of the evaluations to be considered
	 * @param extraEvaluation an evaluation which is temporarily added to the evaluations list. For comparison purposes.
	 * @return A Vector containing the average values of all evaluations with the given name in the order accuracy, precision, recall, specificity.
	 */
	public Vector<Double> getVarianceValues(String name) {
				Vector<Double> avg = getAverageValues (name, null);
                double varianceAcc = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> Math.pow((evaluation.accuracy() 		- avg.get(0)), 2)).average().orElse(Double.NaN);
                double variancePre = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> Math.pow((evaluation.precision() 	- avg.get(1)), 2)).average().orElse(Double.NaN);
                double varianceRec = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> Math.pow((evaluation.recall() 		- avg.get(2)), 2)).average().orElse(Double.NaN);
                double varianceSpe = evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> Math.pow((evaluation.specificity() 	- avg.get(3)), 2)).average().orElse(Double.NaN);
                double varianceTime= evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> Math.pow((evaluation.time			- avg.get(4)), 2)).average().orElse(Double.NaN);
                
                Vector<Double> result = new Vector<Double>();
                result.add(varianceAcc);
                result.add(variancePre);
                result.add(varianceRec);
                result.add(varianceSpe);
                result.add(varianceTime);
                return result;
	}
	

	/**
	 * Returns an evaluation containing the median values of all evaluations with the given name
	 * @param name The name of the evaluations to be considered
	 * @return An evaluation containing the median values of all evaluations with the given name
	 */
	public Evaluation getMedians() {
		// Implemented mostly following https://stackoverflow.com/a/43678244
		List<Double> listTP = evaluations.parallelStream().map(evaluation -> evaluation.TP).sorted().collect(Collectors.toList());
		double medianTP = listTP.size() % 2 == 0?
				listTP.parallelStream().skip((int)(listTP.size() / 2 - 1)).limit(2).mapToDouble(Double::valueOf).average().orElse(Double.NaN):
				listTP.parallelStream().skip((int)(listTP.size() / 2)).findFirst().orElse(Double.NaN);
		
		List<Double> listFN = evaluations.parallelStream().map(evaluation -> evaluation.FN).sorted().collect(Collectors.toList());
		double medianFN = listFN.size() % 2 == 0?
				listFN.parallelStream().skip((int)(listFN.size() / 2 - 1)).limit(2).mapToDouble(Double::valueOf).average().orElse(Double.NaN):
				listFN.parallelStream().skip((int)(listFN.size() / 2)).findFirst().orElse(Double.NaN);
		
		List<Double> listFP = evaluations.parallelStream().map(evaluation -> evaluation.FP).sorted().collect(Collectors.toList());
		double medianFP = listFP.size() % 2 == 0?
				listFP.parallelStream().skip((int)(listFP.size() / 2 - 1)).limit(2).mapToDouble(Double::valueOf).average().orElse(Double.NaN):
				listFP.parallelStream().skip((int)(listFP.size() / 2)).findFirst().orElse(Double.NaN);
				
		List<Double> listTN = evaluations.parallelStream().map(evaluation -> evaluation.TN).sorted().collect(Collectors.toList());
		double medianTN = listTN.size() % 2 == 0?
				listTN.parallelStream().skip((int)(listTN.size() / 2 - 1)).limit(2).mapToDouble(Double::valueOf).average().orElse(Double.NaN):
				listTN.parallelStream().skip((int)(listTN.size() / 2)).findFirst().orElse(Double.NaN);
				
        return new Evaluation("Medians", medianTP, medianFN, medianFP, medianTN);
	}
	
	/**
	 * Returns the average time of all evaluations with the given name
	 * @param name The name of the evaluations to be considered
	 * @return The average time of the considered evaluations
	 */
	public double getAverageTime(String name) {
		return evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> evaluation.time()).average().orElse(Double.NaN);
	}

	/**
	 * Returns the total time of all evaluations with the given name
	 * @param name The name of the evaluations to be considered
	 * @return The total time of the considered evaluations
	 */
	public double getTotalTime(String name) {
		return evaluations.parallelStream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> evaluation.time()).sum();
	}
	
	/**
	 * Returns all evaluations of a single name's values combined into a single evaluation.
	 * @param name the name to filter the evaluations by
	 * @return Evaluation containing the sum of all evaluation's values
	 */
	public kNNEvaluation getSum(String name) {
		double TP = evaluations.stream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> evaluation.TP).sum();
		double FN = evaluations.stream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> evaluation.FN).sum();
		double FP = evaluations.stream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> evaluation.FP).sum();
		double TN = evaluations.stream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> evaluation.TN).sum();
		double time = evaluations.stream().filter(evaluation -> evaluation.name.equals(name)).mapToDouble(evaluation -> evaluation.time).sum();
		return new kNNEvaluation (name + "_", TP, FN, FP, TN, evaluations.get(0).k, time);
		
	}
	
	/**
	 * Shows a JFrame (window) with bar charts for the results of the evaluations
	 */
	public void displayChart() {
		BarChart chart = new BarChart("Measurements", "Measurements", "Category", "Score", this, AnalysisMode.RATINGS);
		chart.show(new Dimension(800, 600));
	}
	
	/**
	 * Returns a pretty, human readable string representing the results
	 * @return a pretty, human readable string representing the results
	 */
	public String getResultsAsHumanReadableString(boolean verbose) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		LocalDateTime timestamp = LocalDateTime.now();
		String s = formatter.format(timestamp);
		s += "\n\nEvaluation Results\n-------------\n";
		for (String name: App.evalName) {
			s += "---\n\n";
			Evaluation evaluation = getAverages (name);
			Evaluation variances = getVariances (name);
			Vector<Double> results = getAverageValues(name, null);
			Vector<Double> resultVariances = getVarianceValues(name);
			s += name + "\n";
//			s += "Acc\tPre\tRec\tSpe\n";
//			s += results.get(0) + "\t" + results.get(1) + "\t" + results.get(2) + "\t" + results.get(3) + "\n\n";
			s += "Accuracy:\t"		+ results.get(0) + " (" + resultVariances.get(0) + ")" + "\n";
			s += "Precision:\t"		+ results.get(1) + " (" + resultVariances.get(1) + ")" + "\n";
			s += "Recall:\t\t"		+ results.get(2) + " (" + resultVariances.get(2) + ")" + "\n";
			s += "Specificity:\t" 	+ results.get(3) + " (" + resultVariances.get(3) + ")" + "\n";
			s += "\n";
			s += "TP:\t" + evaluation.getTP() + " (" + variances.getTP() + ")\n";
			s += "FN:\t" + evaluation.getFN() + " (" + variances.getFN() + ")\n";
			s += "FP:\t" + evaluation.getFP() + " (" + variances.getFP() + ")\n";
			s += "TN:\t" + evaluation.getTN() + " (" + variances.getTN() + ")\n";
			s += "\n";
			s += "Total time:\t" + getTotalTime(name) + "ms\nAvg time:\t" + getAverageTime(name) + "ms (" + resultVariances.get(4) + ")	\n";
			s += "Total number of measurements:\t" + getNumberOfEvaluationsByName(name) + "\n\n";
			
		}
		
		if (verbose) {
			s += "\n\n\nDetailed Results:\n";
			for (kNNEvaluation evaluation: evaluations) {
				s += evaluation.name + "\n";
				s += "TP\tFN\tFP\tTN\ttime (ms)\n";
				s += evaluation.getTP() + "\t" + evaluation.getFN() + "\t"
							+ evaluation.getFP() + "\t" + evaluation.getTN() + "\t"
							+ evaluation.time() + "\n\n";
			}
		}
		
		return s;
	}
	
	/**
	 * Saves a pretty, human readable string to a given file
	 * @param file file path of the file the string should be saved in
	 */
	public void saveResultsAsHumanReadableFile(String file) {
		FileWriter writer;
		try {
			writer = new FileWriter (file);
			writer.append(getResultsAsHumanReadableString(true));
			
		} catch (IOException e) {
			System.err.println ("Couldn't save results!");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Saves the values of the evaluations as CSV.
	 * @param file The path of the CSV file (will be created if non-existent)
	 */
	public void saveAsCSV(String file) {
		FileWriter writer;
		try {
			writer = new FileWriter(file);
			writer.append(evaluations.get(0).getCSVHeader());
			writer.append("\n");
			for (Evaluation evaluation: evaluations) {
				writer.append(evaluation.getCSVRepresentation());
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			System.err.println("Something went wrong when saving the results as CSV");
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads old evaluations from a CSV file. Useful for direct comparison using displayChart().
	 * @param file The CSV file to be loaded from
	 * @param evalNamePrefix String to be prepended to all loaded evaluations to avoid name clashes.
	 */
	public void loadFromCSV(String file, String evalNamePrefix) {
		FileReader in;
		try {
			in = new FileReader(file);
			CSVParser csvParser = CSVFormat.EXCEL.withFirstRecordAsHeader().parse(in);
			for (CSVRecord record : csvParser) {
				kNNEvaluation evaluation = (kNNEvaluation)Evaluation.fromRecord(record, evalNamePrefix);
				addEvaluation(evaluation); 
			}
			in.close();
		} catch (FileNotFoundException e) {
			System.err.println("Could not find file " + file);
			System.err.println("No evaluations were loaded");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Something went wrong while reading evaluations from file " + file);
			System.err.println("No evaluations were loaded");
			e.printStackTrace();
		}		
	}
	
	
	/**
	 * Checks whether the new evaluation's impact on the total of the previous evaluations is smaller than a given delta
	 * @param evaluation New evaluation to compare with the previous evaluations
	 * @param delta A number delta in |oldEvaluations.S - evaluation.S < delta| for each stat. measure S
	 * @param evalNDelta The number of successive times this function has a positive check in a row for it to return true
	 * @return True if the last evalNDelta times this function was called the above formula held true, false otherwise.
	 */
	public boolean checkEvaluationDelta (kNNEvaluation evaluation, double delta, int evalNDelta) {
		// If no resampling was done, simply return true
		// - in this case, there is no randomness and thus no need for multiple runs
		if (App.mode == App.modes.NoResampling) {
			return true;
		}
		
		if (evaluations.stream().filter(ev-> ev.name.equals(evaluation.name)).count() < 2) {
			return false;
		}
		

		Vector<Double> newValues = getAverageValues(evaluation.name, evaluation);
		Vector<Double> oldValues = getAverageValues(evaluation.name, null);
		double accDelta = Math.abs (newValues.get(0) - oldValues.get(0));
		double prcDelta = Math.abs (newValues.get(1) - oldValues.get(1));
		double recDelta = Math.abs (newValues.get(2) - oldValues.get(2));
		double spcDelta = Math.abs (newValues.get(3) - oldValues.get(3));
		if (App.verbose) {
			System.out.println ("-> -> All evaluations:\naccDelta = " + accDelta + "\nprcDelta = " + prcDelta + "\nrecDelta = " + recDelta + "\nspcDelta = " + spcDelta + "\n(Target: " + delta + ")"); 
		}
		if (accDelta < delta && prcDelta < delta && recDelta < delta && spcDelta < delta) {
			// If there aren't enough hits in a row yet, increase counter and return false
			if (evalN < evalNDelta) {
				evalN++;
				System.out.println ("n: " + evalN + "/" + evalNDelta);
				return false;
			}
			// Else, reset hit counter and return true
			evalN = 0;
			if (App.verbose) {
				 System.out.println ("Evaluations done!");
				System.out.println ("Average time: " + getAverageTime(evaluation.name));
			}
			return true;
		}
		else {
			evalN = 0;
		}
		return false;
	}
}

