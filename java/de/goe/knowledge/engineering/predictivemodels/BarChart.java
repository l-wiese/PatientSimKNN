package de.goe.knowledge.engineering.predictivemodels;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;


enum AnalysisMode {
	VALUES,
	RATINGS
}

class BarChart extends JFrame {
	/**
	 * Helper class which creates the Swing/JFreeChart-window and displays a bar chart
	 */
	
	private static final long serialVersionUID = 1L;
	
	AnalysisMode analysisMode;
	DefaultCategoryDataset dataset;
	JFreeChart chart;
	/**
	 * Constructor. Instantiating the class is enough to create a JFrame (window) showing evaluation results.
	 * @param applicationTitle The title of the application
	 * @param chartTitle The title of the chart (shown directly above the chart)
	 * @param xLabel Label of the x-axis of the chart
	 * @param yLabel Label of the y-axis of the chart
	 * @param analyzer 	Analyzer class containing all evaluations to be shown. 
	 * 					Usually the one that is instantiating this class.
	 */
	public BarChart(String applicationTitle, String chartTitle, String xLabel, String yLabel, Analyzer analyzer, AnalysisMode analysisMode) {
		super(applicationTitle);
		dataset = new DefaultCategoryDataset();
		this.analysisMode = analysisMode;
		setDataset (analyzer.getEvaluations());
		
		chart = ChartFactory.createBarChart(
				chartTitle,
				xLabel,
				yLabel,
				dataset,
				PlotOrientation.VERTICAL,
				true, true, false
				);
	}
	
	/**
	 * Displays the window containing the bar chart.
	 * @param dimension The size of the window
	 */
	public void show(Dimension dimension) {
		ChartPanel panel = new ChartPanel(chart);
		panel.setPreferredSize(dimension);
		setContentPane(panel);
		pack();
		setVisible(true);
	}
	
	/**
	 * Sets the values of the JFreeChart data set to those of the evaluations.
	 * @param evaluations Vector with evaluations (usually through analyzer.getEvaluations())
	 */
	public void setDataset(Vector<kNNEvaluation> evaluations) {
		for (Evaluation evaluation : evaluations) {
			addToDataset(evaluation);
		}
	}
	
	/**
	 * Adds the values from an evaluation to the data set
	 * @param evaluation The evaluation containing the values
	 */
	public void addValuesToDataset(Evaluation evaluation) {
		dataset.addValue(evaluation.TP, evaluation.name, "TP");
		dataset.addValue(evaluation.FP, evaluation.name, "FP");
		dataset.addValue(evaluation.TN, evaluation.name, "TN");
		dataset.addValue(evaluation.FN, evaluation.name, "FN");
	}
	
	
	/**
	 * Adds the ratings from an evaluation to the data set
	 * @param evaluation The evaluation containing the ratings
	 */
	public void addRatingsToDataset(Evaluation evaluation) {
		dataset.addValue(evaluation.accuracy(), evaluation.name, "accuracy");
		dataset.addValue(evaluation.specificity(), evaluation.name, "specificity");
		dataset.addValue(evaluation.precision(), evaluation.name, "precision");
		dataset.addValue(evaluation.recall(), evaluation.name, "recall");
	}
	
	/**
	 * Adds values or ratings (depending on mode) from an evaluation to the data set
	 * @param evaluation The evaluation containing the values/ratings
	 * @param mode Whether to pick values (AnalysisMode.VALUES) or ratings (AnalysisMode.RATINGS)
	 */
	public void addToDataset(Evaluation evaluation) {
		switch (analysisMode) {
			case VALUES: 
				addValuesToDataset(evaluation);
				break;
			
			case RATINGS:
				addRatingsToDataset(evaluation);
				break;
		}
	}
}
