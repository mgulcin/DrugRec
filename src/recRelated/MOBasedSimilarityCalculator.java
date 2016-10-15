package recRelated;

import java.util.ArrayList;
import java.util.List;

public class MOBasedSimilarityCalculator {


	protected Double doesDominate(Similarity s1, Similarity s2, 
			ArrayList<Similarity.SimType> featuresToUse) {
		/*
		 *  In order to be dominated a solution must have a 
		 *  �score� of 0 in pairwise comparison.
		 *  
		 *  Pairwise comp: Compare elements of similarity 
		 *  st s1.FeatureScore > s2.FeatureScore --> s1.DomScore = 1 s2.DomScore = 0
		 *     s1.FeatureScore < s2.FeatureScore --> s1.DomScore = 0 s2.DomScore = 1
		 *      s1.FeatureScore == s2.FeatureScore --> s1.DomScore = 0 s2.DomScore = 0 ??
		 *      
		 *  If s1.DomScore > s2.DomScore && s2.DomScore = 0 --> return 1
		 *  Else return 0
		 */
		Double s1DomScore = 0.0;
		Double s2DomScore = 0.0;

		// compare s1 and s2
		int simTypecount = featuresToUse.size();
		for(int i = 0; i< simTypecount; i++)
		{
			Similarity.SimType key = featuresToUse.get(i);
			Double score1 = s1.getSimilarityMap().get(key);
			Double score2 = s2.getSimilarityMap().get(key);

			if(score1 != null && score2!= null &&
					score1.compareTo(score2) > 0){
				s1DomScore += 1;
			}
			else if(score1 != null && score2!= null &&
					score1.compareTo(score2) < 0){
				s2DomScore += 1;
			} 
//			else if(score1 != null && score2==null){
//				s1DomScore += 1;
//			} else if(score2 != null && score1==null){
//				s2DomScore += 1;
//			}
			
			
//			else {
//				// (at least)one of the scores is null
//				// do nothing
//				//TODO may behave as if non-null dominates the null one
//			}
		}

		// If s1.DomScore > s2.DomScore && s2.DomScore = 0 --> return 1
		// Else return 0;
		Double dominanceVal = 0.0;
		if(s1DomScore.compareTo(s2DomScore) > 0 && s2DomScore == 0)
		{
			dominanceVal = 1.0;
		}

		return dominanceVal;
	}



	public Double[][] createDominanceMatrix(List<Similarity> geneSimValsMap, 
			ArrayList<Similarity.SimType> fieldsToUse) {
		// create dominanceMatrix
		int size = geneSimValsMap.size();// number of other genes (non-target) 
		Double[][] dominanceMatrix = new Double[size][size];

		// set dominanceMatrix
		int rowSize = size;
		int columnSize = size;
		for(int i=0; i<rowSize; i++)
		{
			Similarity s1 = geneSimValsMap.get(i);

			for(int j=0; j<columnSize; j++)
			{
				Similarity s2 = geneSimValsMap.get(j);
				

				Double val = doesDominate(s1,s2, fieldsToUse);

				
				dominanceMatrix[i][j] = val;
			}
		}

		return dominanceMatrix;
	}
	
	public ArrayList<Similarity> findNonDominatedSims(
			List<Similarity> geneSimValsMap, Double[][] dominanceMatrix) {
		/*
		 * Non-dominated solutions have a zero in the column total
		 */	
		// get colmn totals
		int columnSize = geneSimValsMap.size();
		int rowSize = columnSize;
		Double[] colmTotals = new Double[columnSize];
		for(int j=0; j<columnSize; j++)
		{
			Double colnTotal = 0.0;
			for(int i=0; i<rowSize; i++)
			{
				colnTotal += dominanceMatrix[i][j];
			}

			colmTotals[j] = colnTotal;
		}

		// create the returned list -- may do this in the prev loop.
		ArrayList<Similarity> nonDominatedSims  = new ArrayList<Similarity>();
		for(int j=0; j<columnSize; j++)
		{
			if(colmTotals[j].equals(0.0))
			{
				Similarity sim = geneSimValsMap.get(j);
				nonDominatedSims.add(sim);
			}
		}

		return nonDominatedSims;
	}

}
