# PatientSimKNN
Patient Similarity Analysis for Mortality Prediction

# Methods
To evaluate the reliability of different ML frameworks for hospitals use, we
focus on the application of k Nearest Neighbour (kNN) and identifying optimal
settings of the algorithm on the freely available MIMIC-III data set (Johnson
et al, 2016). We aim to predict the mortality of patients utilizing
the two well-known Java frameworks ELKI (Schubert et al, 2015) and Weka
(Witten et al, 2016).

The employed machine learning tools offer several distance implementations.
We chose three common distance measures: Manhattan and Euclidean of the Minkowski family as well as the Cosine Distance.

To figure out which framework as well as which k and distance measure to
choose to gain the most accurate and reliable kNN model, we calculated dif-
ferent performance metrics (accuracy, precision, recall and specificity) from the confusion matrices resulting from our prediction models. The positive/majority class in our data
set is the set of surviving patients; the negative/minority class is the set of
deceased patients. Hence, the true positives (TP) are all correctly classified sur-
viving patients, while true negatives (TN) are all correctly classified deceased
patients. Correspondingly, the false positives (FP) are all patients classified as
surviving although they actually died; the false negatives (FN) are all patients
classified as deceased although they actually survived.


To estimate the accuracy of a classification model we performed stratified 10-
fold cross-validation. It splits the data set into 10 equally sized random subsets,
so called folds. The stratification ensures that each fold has approximately the
same proportion of deceased and alive patients as the original MIMIC-III data
set.

#Results

We evaluated the use case of mortality prediction with ELKI
and Weka in terms of their implementation effort as well as their classification
performance for kNN. Using these two Java frameworks is a fast and easy way
to implement complex classifications. This may enable health care systems to
implement their own analysis systems and improve decision making. Still, the
frameworks we used behave like black-boxes since the implementation of the
algorithms is not obvious and the documentation is sparse.
For the MIMIC-III dataset the ideal case was oversampling on the entire
dataset. In our tests, ELKI was costlier to implement but had a better runtime
than Weka. To even get a better specificity, undersampling with only the top
20 features could be chosen which at the same time has the lowest runtime.