package de.goe.knowledge.engineering.predictivemodels;


/***
 * 
 * @author christoph
 * @see de.goe.knowledge.engineering.predictivemodels.Evaluation
 * Class derived from Evaluation which also saves the k in kNN and the time needed by the calculation
 *
 */
public class kNNEvaluation extends Evaluation {
	int k;
	public double time;
	
	
	/**
	 * Empty constructor which overrides the parent's constructor
	 */
	public kNNEvaluation(String name) {
		super(name);
		this.k = 0;
		this.time = -1;
	}
	
	/**
	 * Constructor which overrides the parents constructor and adds k and time as values.
	 * @param name Name of the evaluation
	 * @param TP True Positives measured
	 * @param FN False Negatives measured
	 * @param FP False Positives measured
	 * @param TN True Negatives measured
	 * @param k The k chosen for kNN
	 * @param time The time the algorithm took
	 */
	public kNNEvaluation(String name, double TP, double FN, double FP, double TN, int k, double time) {
		super(name, TP, FN, FP, TN);
		this.k = k;
		this.time = time;
	}
	
	/**
	 * Constructor which overrides the parents constructor and adds k and time as values.
	 * Initializes time as -1 for cases where the result of the timer is available only after construction.
	 * Time can be modified directly from outside (using kNNEvaluation.time).
	 * @param name Name of the evaluation
	 * @param TP True Positives measured
	 * @param FN False Negatives measured
	 * @param FP False Positives measured
	 * @param TN True Negatives measured
	 * @param k The k chosen for kNN
	 */
	public kNNEvaluation(String name, double TP, double FN, double FP, double TN, int k) {
		super(name, TP, FN, FP, TN);
		this.k = k;
		time = -1;
	}
	
	
	/**
	 * Returns the saved time as nano seconds.
	 * @return The time in nano seconds
	 */
	public double time() {
		return time * Math.pow(10, -6);
	}
	
	
	/**
	 * Returns a representation for saving the evaluation in a CSV.
	 * @return The representation
	 * @see de.goe.knowledge.engineering.predictivemodels.Analyzer
	 * @see de.goe.knowledge.engineering.predictivemodels.Evaluation
	 */
	public String getCSVRepresentation() {
		return name + "," + TP + "," + FN + ","	+ FP + ","	+ TN
					+ "," + accuracy() + "," + precision() + "," + recall() + "," + specificity()
					+ ", "+ k + "," + time + "\n";
	}
	/**
	 * Returns the header for saving the evaluation in a CSV.
	 * Overwrite this for classes that inherit this one.
	 * @return The representation
	 * @see de.goe.knowledge.engineering.predictivemodels.Analyzer
	 */
	public String getCSVHeader() {
		return "Name,TP,FN,FP,TN,accuracy,precision,recall,specificity,k,time";
	}
	
	
	/**
	 * Adds values to the current evaluation
	 * @param TP Extra TP value
	 * @param FN Extra FN value
	 * @param FP Extra FP value
	 * @param TN Extra TN value
	 * @param time
	 */
	public void addValuesToEvaluation (double TP, double FN, double FP, double TN, double time) {
		super.addValuesToEvaluation(TP, FN, FP, TN);
		this.time += time;
	}
	
	/**
	 * Adds the values of a given evaluation to the current evaluation.
	 * @param evaluation The given evaluation whose values are added to this evaluation.
	 */
	public void addValuesToEvaluation (kNNEvaluation evaluation) {
		super.addValuesToEvaluation(evaluation);
		this.time += evaluation.time;
		this.k = evaluation.k;
	}
	
	/**
	 * Prints a nice representation of the kNN evaluation to stdout.
	 */
	public void print() {
		String str = toString() + "\nAccuracy\t" + accuracy() + "\nPrecision\t"
					+ precision() + "\nRecall\t\t" + recall() + "\nSpecificity\t"
					+ specificity() + "\nTime (ms)\t" + time();
		System.out.println(str);
	}
	
}
