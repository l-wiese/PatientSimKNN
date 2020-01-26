package de.goe.knowledge.engineering.predictivemodels;

public class Evaluation {

	double TP, FP, FN, TN;
	double size = 32635;

	public Evaluation(double TP, double FN, double FP, double TN) {
		this.TP = TP;
		this.FP = FP;
		this.FN = FN;
		this.TN = TN;

		System.out.println("\n" + "TP " + TP + "\t FN " + FN + "\t FP " + FP + " \t TN " + TN);

		double sum = TP + FP + FN + TN;
		if (sum != size) {
			try {
				throw new Exception();
			} catch (Exception e) {
				System.err.println("Does not the expected size of " + size + " and is instead " + sum);
			}
		}
	}

	public void accuracy() {
		double right = TP + TN;
		double wrong = TP + FP + TN + FN;
		double result = (right / wrong);
		System.out.println("ACCURACY " + result);
	}

	public void precision() {
		double rest = TP + FP;
		double result = TP / rest;
		System.out.println("PRECISION " + result);
	}

	public void recall() {
		double rest = TP + FN;
		double result = TP / rest;
		System.out.println("RECALL " + result);
	}

	public void specificity() {
		double rest = FP + TN;
		double result = TN / rest;
		System.out.println("SPECIFICITY " + result);
	}
}
