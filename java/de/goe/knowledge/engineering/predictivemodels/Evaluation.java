package de.goe.knowledge.engineering.predictivemodels;

import org.apache.commons.csv.CSVRecord;

public class Evaluation {

	String name;
	double TP, FP, FN, TN;
	double size = 32635;
	boolean saveable;	// Whether the evaluation can be saved or not (to prevent re-saving of loaded evaluations)
	
	public Evaluation(String name) {
		this.name = name;
		this.TP = this.FP = this.FN = this.TN = 0;
	}
	
	public Evaluation(String name, double TP, double FN, double FP, double TN) {
		this.name = name;
		this.TP = TP;
		this.FP = FP;
		this.FN = FN;
		this.TN = TN;
		saveable = true;
	}
	
	public double getTP() {
		return TP;
	}
	
	public double getFN() {
		return FN;
	}
	
	public double getFP() {
		return FP;
	}
	
	public double getTN() {
		return TN;
	}
	
	
	/**
	 * Add values to this evaluation
	 * @param TP Extra TP
	 * @param FN Extra FN
	 * @param FP Extra FP
	 * @param TN Extra TN
	 */
	public void addValuesToEvaluation (double TP, double FN, double FP, double TN) {
		this.TP += TP;
		this.FN += FN;
		this.FP += FP;
		this.TN += TN;
	}
	
	/**
	 * Add values of another evaluation to this evaluation
	 * @param evaluation The other evaluation
	 */
	public void addValuesToEvaluation (Evaluation evaluation) {
		addValuesToEvaluation  (evaluation.getTP(),
								evaluation.getFN(),
								evaluation.getFP(),
								evaluation.getTN());
	}

	
	/**
	 * Returns this evaluation's accuracy
	 * @return this evaluation's accuracy
	 */
	public double accuracy() {
		double right = TP + TN;
		double wrong = TP + FP + TN + FN;
		return (right / wrong);
	}

	/**
	 * Returns this evaluation's precision
	 * @return this evaluation's precision
	 */
	public double precision() {
		double rest = TP + FP;
		return TP / rest;
	}

	/**
	 * Returns this evaluation's recall
	 * @return this evaluation's recall
	 */
	public double recall() {
		double rest = TP + FN;
		return TP / rest;
	}

	/**
	 * Returns this evaluation's specificity
	 * @return this evaluation's specificity
	 */
	public double specificity() {
		double rest = FP + TN;
		return TN / rest;
	}
	
	/**
	 * Prints a nice representation of the evaluation to stdout.
	 */
	public void print() {
		String str = toString() + "\nAccuracy\t" + accuracy() + "\nPrecision\t"
					+ precision() + "\nRecall\t\t" + recall() + "\nSpecificity\t" + specificity();
		System.out.println(str);
	}
	
	/**
	 * Sets whether this evaluation will be saved in the CSV by Analyzer when Analyzer.SaveAsCSV is called
	 * @param saveable
	 */
	public void setSaveable(boolean saveable) {
		this.saveable = saveable;
	}
	
	
	/**
	 * Returns the value of this evaluation subtracted by the given evaluation
	 * @param evaluation the evaluation to subtract
	 * @return the result of the subtraction
	 */
	public Evaluation subtract(Evaluation evaluation) {
		return new Evaluation (name + "-" + evaluation.name,
								this.TP - evaluation.getTP(),
								this.FN - evaluation.getFN(),
								this.FP - evaluation.getFP(),
								this.TN - evaluation.getTN());
	}
	
	/**
	 * Returns a representation for saving the evaluation in a CSV.
	 * Overwrite this for classes that inherit this one.
	 * @return The representation
	 * @see de.goe.knowledge.engineering.predictivemodels.Analyzer
	 */
	public String getCSVRepresentation() {
		return name + "," + TP + "," + FN + ","	+ FP + ","	+ TN
					+ "," + accuracy() + "," + precision() + "," + recall() + "," + specificity() + "\n";
	}
	
	/**
	 * Returns the header for saving the evaluation in a CSV.
	 * Overwrite this for classes that inherit this one.
	 * @return The representation
	 * @see de.goe.knowledge.engineering.predictivemodels.Analyzer
	 */
	public String getCSVHeader() {
		return "Name,TP,FN,FP,TN,accuracy,precision,recall,specificity";
	}
	
	@Override
	public String toString() {
		return ("Evaluation:\nTP\t" + TP + "\nFP\t" + FP + "\nTN\t" + TN + "\nFN\t" + FN + "\n");
	}
	
	/**
	 * Static method which constructs an evaluation from a given CSVRecord
	 * @param record The CSVRecord the evaluation is to be constructed from
	 * @param evalNamePrefix Prefix of the evaluation's name (useful for avoiding duplicated names when loading from files)
	 * @return The created evaluation
	 */
	public static Evaluation fromRecord(CSVRecord record, String evalNamePrefix) {
		String name = evalNamePrefix + record.get("Name");
		double TP = Double.parseDouble(record.get("TP"));
		double FN = Double.parseDouble(record.get("FP"));
		double FP = Double.parseDouble(record.get("TN"));
		double TN = Double.parseDouble(record.get("FN"));
		Evaluation evaluation = new Evaluation(name, TP, FN, FP, TN);
		evaluation.setSaveable(false);
		return evaluation;
	}
}
