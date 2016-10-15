package main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import drugRelated.Item;
import method.DrugRecommender;
import method.DrugRecommenderParameters;
import method.DrugRecommenderParameters.ISType;
import method.DrugRecommenderParameters.MOType;
import method.DrugRecommenderParameters.OutlistType;
import recRelated.Rating;
import recRelated.Recommendation;
import recRelated.Similarity.SimType;
import util.FilePrinter;
import util.ItemReader;
import util.Printer;
import util.Utils;



public class MainDrugRec {
	// parameters

	protected static Printer printer = new FilePrinter(false);
	protected static String inputPath = ".//data//";
	protected static String outputPath = ".//output//";//output: moNullNot used look for k


	protected static String allDrugsPath = ".//data//allDrugs.txt";
	protected static String allDiseasesPath = ".//data//allDiseases.txt";
	protected static String drugDiseasePath = ".//data//drug_disease.tsv";
	protected static String drugDiseaseSubsetPath = ".//data//prepData//train.tsv";
	protected static String targetDrugsPath = ".//data//prepData//subData_drug_disease.tsv";
	
	public static Set<Item> allDrugs = null;
	public static Set<Item> allDiseases = null;
	private static Integer MAX_OUTLIST_SIZE = 35; // 729 disease var
	private static Integer MAX_NEIGHBOR_COUNT = 20;//1007 drug var
	
	
	static boolean useRandomValsOnly = false;
	private static Integer RANDOM_NEIGHBOR_COUNT = 5;// random count
	private static Integer RANDOM_OUTLIST_SIZE = 5;// random count

	// ratingscore=1 for all drug-disease, no need to perform threshold based
	static boolean useThresholdBasedOutput = false;
	// avg is always 1, since ratingscore=1 for all drug-disease
	static boolean useAvgBasedScoreCalc = false; 
	// max is always 1, since ratingscore=1 for all drug-disease
	static boolean useMaxBasedScoreCalc = false; 
	// weighted avg is always 1, since ratingscore=1 for all drug-disease (no weight)
	static boolean useWeightedAvgBasedScoreCalc = false; 

	public static void main(String[] args) {
		
		try {
			allDrugs = ItemReader.readAllItems(allDrugsPath);
			allDiseases = ItemReader.readAllItems(allDiseasesPath);
			Set<Item> targets = ItemReader.readTargets(targetDrugsPath, allDrugs);
			Map<Item, List<Rating>> drugDiseaseMap = ItemReader.readKnownDrugDiseaseMap(drugDiseasePath,allDrugs, allDiseases);
			//Map<Item, List<Rating>> drugDiseaseMap = ItemReader.readKnownDrugDiseaseMap(drugDiseaseSubsetPath,allDrugs, allDiseases);
			
			System.out.println("#targets: " + targets.size());
			
			// targets can be a subset of alldrugs
			Set<Item> targetDrugs = targets;//allDrugs;


			// create recommender
			DrugRecommender recMethod = new DrugRecommender(inputPath, outputPath);

			// create  parameters
			List<DrugRecommenderParameters> parameterList = createParameters();

			// Set sim-fields to be used
			//TODO Make field selection dynamic
			ArrayList<SimType> simFieldList = new ArrayList<SimType>();
			simFieldList.add(SimType.PROTEIN);
			simFieldList.add(SimType.STRUCTURE);
			simFieldList.add(SimType.SIDER);
			//simFieldList.add(SimType.DRUG);
			
			Set<Integer> NUMBER_OF_NEIGHBORS = new HashSet<Integer>();
			NUMBER_OF_NEIGHBORS.add(-1);
			NUMBER_OF_NEIGHBORS.add(1);
			NUMBER_OF_NEIGHBORS.add(4);
			//NUMBER_OF_NEIGHBORS.add(5);
			NUMBER_OF_NEIGHBORS.add(8);
			//NUMBER_OF_NEIGHBORS.add(9);
			NUMBER_OF_NEIGHBORS.add(12);
			NUMBER_OF_NEIGHBORS.add(16);
			NUMBER_OF_NEIGHBORS.add(20);
			
			
			Set<Integer> NUMBER_OF_PREDICTION = new HashSet<Integer>();
			NUMBER_OF_PREDICTION.add(-1);
			NUMBER_OF_PREDICTION.add(1);
			NUMBER_OF_PREDICTION.add(4);
			NUMBER_OF_PREDICTION.add(8);
			NUMBER_OF_PREDICTION.add(12);
			NUMBER_OF_PREDICTION.add(16);
			NUMBER_OF_PREDICTION.add(20);
			
			// set parameters
			for(DrugRecommenderParameters recParameters:parameterList){
				// if parameter is somehow restricted do not use it for rec.
				if(isRestricted(recParameters) == true){
					continue;
				}

				// TODO kaldir sonra
				if(NUMBER_OF_NEIGHBORS.contains(recParameters.getNumberOfSimilarItems()) == false){
					continue;
				}
				
				// TODO kaldir sonra
				if(NUMBER_OF_PREDICTION.contains(recParameters.getOutputListSize()) == false){
					continue;
				}
				
				
				// control if file exist
				String recPathTemp = outputPath + "recItems," + 
						recParameters.toString() + ".tsv";
				File f = new File(recPathTemp);
				if(f.exists() && !f.isDirectory()) { 
					continue;
				}

				recMethod.setParameters(recParameters);
				// run experiments
				for(Item targetDrug: targetDrugs){	
					ArrayList<Recommendation> recommendedItems = recMethod.recommend(targetDrug,
							simFieldList, drugDiseaseMap, allDiseases);

					// Print recommended items
					String recOutPath = outputPath + "recItems," + 
							recParameters.toString() + ".tsv";
					printer.printRecommendeds(recOutPath, targetDrug.getNames().toString(), recommendedItems);
				}
			}

		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public static boolean isRestricted(DrugRecommenderParameters recParameters) {
		boolean retVal = false;
		if(useRandomValsOnly == true){
			// If want to test on random vals for neighbor count and
			// outlist size, set useRandomValsOnly to true

			// there are some exceptions to general rule			
			if(retVal == false && recParameters.getOutlistType() == OutlistType.THRESHOLDBASED && 
					recParameters.getOutputListSize() == -1){
				// thresholdbased outlist--> no restriction on output list size
				if(recParameters.getNumberOfSimilarItems() != RANDOM_NEIGHBOR_COUNT ){
					retVal = true;
				}
			} else if(retVal == false && recParameters.getPrefferedMoType() == MOType.ONLYDOMINATES && 
					recParameters.getNumberOfSimilarItems()  == -1){
				// mo type is  only dominates (1-pass) --> no restriction on neighbor count
				if(recParameters.getOutputListSize() != RANDOM_OUTLIST_SIZE){

					retVal = true;
				}
			} else if(retVal == false && recParameters.getNumberOfSimilarItems() != RANDOM_NEIGHBOR_COUNT ||
					recParameters.getOutputListSize() != RANDOM_OUTLIST_SIZE){
				// general rule 
				retVal = true;
			}

		} 
		
		if(retVal == false && useThresholdBasedOutput == false && 
				recParameters.getOutlistType().equals(OutlistType.THRESHOLDBASED)){
			retVal = true;
		} 
		
		if(retVal == false && useAvgBasedScoreCalc == false && 
				recParameters.getItemSelectionType().equals(ISType.AVG)){
			retVal = true;
		} 
		
		if(retVal == false && useMaxBasedScoreCalc == false && 
				recParameters.getItemSelectionType().equals(ISType.MAX)){
			retVal = true;
		} 
		
		if(retVal == false && useWeightedAvgBasedScoreCalc == false && 
				recParameters.getItemSelectionType().equals(ISType.WEIGHTEDAVG)){
			retVal = true;
		} 

		return retVal;
	}


	private static  List<DrugRecommenderParameters> createParameters() {
		// defult parameters
		MOType moTypeDefault = MOType.ONLYDOMINATES;
		ISType isTypeDefault = ISType.SUM;
		OutlistType outlistTypeDefault = OutlistType.THRESHOLDBASED;

		Integer numberOfSimilarDrugsDefault = -1;
		Integer outputListSizeDefault = -1; 
		Double thresholdDefault = -1.0;

		// retList
		List<DrugRecommenderParameters> parameterList = new ArrayList<DrugRecommenderParameters>();

		// loop over parameters
		for(MOType moType: MOType.values()){
			for(ISType isType: ISType.values()){
				for(OutlistType outlistType: OutlistType.values()){

					if(moType.equals(MOType.ONLYDOMINATES)){
						// no need to change number of similar genes
						setByOutlistType(outlistType, parameterList, numberOfSimilarDrugsDefault,
								outputListSizeDefault,thresholdDefault, moType, isType);

					} else{
						// change number of similar genes
						for(Integer noSimGenes = 1; noSimGenes <= MAX_NEIGHBOR_COUNT ;  noSimGenes++){
							setByOutlistType(outlistType, parameterList, noSimGenes,
									outputListSizeDefault,thresholdDefault, moType, isType);

						}
					}

				} // end of the for loop: outlistType
			} // end of the for loop: isType
		} // end of the for loop: moType

		return parameterList;

	}

	private static void setByOutlistType(OutlistType outlistType, 
			List<DrugRecommenderParameters> parameterList, 
			Integer noSimDrugs, Integer outputListSizeDefault, 
			Double thresholdDefault, MOType moType, ISType isType) {
		// no need to set numSimGenes
		if(outlistType.equals(OutlistType.FIXEDLENGTH)){
			// no need to change thresholds
			for(Integer outListSize = 1; outListSize <= MAX_OUTLIST_SIZE ;  outListSize++){
				DrugRecommenderParameters gParameters = 
						new DrugRecommenderParameters(noSimDrugs,
								outListSize, thresholdDefault, 
								moType, outlistType, isType);
				parameterList.add(gParameters);
			}
		} else if(outlistType.equals(OutlistType.THRESHOLDBASED)){
			// no need to change outlist size
			int decimalPlaces = 2; 
			for(Double threshold = 0.51; threshold <= 1.0;  threshold+=0.03){
				BigDecimal bd = new BigDecimal(threshold);
				bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP);
				threshold = bd.doubleValue();           

				DrugRecommenderParameters gParameters = 
						new DrugRecommenderParameters(noSimDrugs,
								outputListSizeDefault, threshold, 
								moType, outlistType, isType);
				parameterList.add(gParameters);
			}	
		}

	}
}