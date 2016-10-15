package eval;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import drugRelated.Item;
import method.DrugRecommenderParameters;
import method.DrugRecommenderParameters.ISType;
import method.DrugRecommenderParameters.MOType;
import method.DrugRecommenderParameters.OutlistType;
import recRelated.Rating;
import util.FilePrinter;
import util.ItemReader;
import util.Printer;
import util.Utils;



public class Evaluate {

	static String resultFolderStr = ".//output-jjs//";
	static String evalResultFolderStr = ".//evalResult-jjs//";
	
	protected static String allDrugsPath = ".//data//allDrugs.txt";
	protected static String allDiseasesPath = ".//data//allDiseases.txt";
	protected static String drugDiseasePath = ".//data//drug_disease.tsv";
	//protected static String drugDiseasePath = ".//data//prepData//test.tsv";
	static Set<Item> allDrugs;
	static Set<Item> allDiseases;

	static Printer printer = new FilePrinter(false);
	
	public static void main(String[] args) {
		
		// read golden nw data : targetItem --> list of <recommenderDrug, recommendedDisease, ratingScore>
		allDrugs = ItemReader.readAllItems(allDrugsPath);
		allDiseases = ItemReader.readAllItems(allDiseasesPath);
		Map<Item, List<Rating>> goldenDrugDiseaseMap = ItemReader.readKnownDrugDiseaseMap(drugDiseasePath,allDrugs, allDiseases);

		// evaluate
		// the output path of recommender
		evaluate(resultFolderStr, goldenDrugDiseaseMap);


		//get overall measures
		getOverallResults();
		
		
//		evaluateKarsiTakim(resultFolderStr, goldenDrugDiseaseMap);
//		getOverallResultsKarsiTakim();

	}


	private static void getOverallResults() {
		// get files in result folder
		try{

			File evalResultFolder = new File(evalResultFolderStr);
			File[] listOfFiles = evalResultFolder.listFiles(); 

			for(File evalFile:listOfFiles){
				String name = evalFile.getName();
				if(name.startsWith("evalResult")){
					// decide on parameters
					DrugRecommenderParameters params = getParams(name);

					// get overall results
					getOverallResults(evalFile, params);

				}
			}

		} catch(Exception e){
			e.printStackTrace();
		}


	}


	private static void getOverallResultsKarsiTakim() {
		// get files in result folder
		try{

			File evalResultFolder = new File(evalResultFolderStr);
			File[] listOfFiles = evalResultFolder.listFiles(); 

			for(File evalFile:listOfFiles){
				String name = evalFile.getName();
				if(name.startsWith("evalResult")){
					// get overall results
					getOverallResultsKarsiTakim(evalFile);

				}
			}

		} catch(Exception e){
			e.printStackTrace();
		}


	}


	
	private static void getOverallResults(File evalFile, DrugRecommenderParameters parameters) {

		// read file , get total number of tp,fp,tn,fn, prec,recall,f1
		Double totalTp = 0.0;
		Double totalFp = 0.0;
		Double totalTn = 0.0;
		Double totalFn = 0.0;
		
		try{
			// Open the file
			FileInputStream fstream = new FileInputStream(evalFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			//Read File Line By Line until the index of target
			String strLine = null;
			while ((strLine = br.readLine()) != null)   // read info
			{
				// read related line, parse it and return output
				// Target \t TP,FP,TN,FN,Precision,Recall,F1Measure
				//E.g. [triamcinolone]	3.0, 2.0, 0.0, 9.0, 0.6, 0.25, 0.35294117647058826
				String[] splittedEvalResult = strLine.split("\t");
				String targetDrug = splittedEvalResult[0];
				String evalResultStr = splittedEvalResult[1];
				
				String[] splitted = evalResultStr.split(",");

				Double tp = Double.valueOf(splitted[0].trim());
				Double fp = Double.valueOf(splitted[1].trim());
				Double tn = Double.valueOf(splitted[2].trim());
				Double fn = Double.valueOf(splitted[3].trim());
				

				totalTp += tp;
				totalFp += fp;
				totalTn += tn;
				totalFn += fn;
				
			}

			// calculate evalRes
			EvaluationResult overallEvalRes = new EvaluationResult(totalTp, totalFp, totalTn, totalFn);
			

			// printResults
			String resultPath = evalResultFolderStr + "//OverallResult.csv";
			printer.printOverallEvalResult(resultPath, parameters, overallEvalRes);


		} catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void getOverallResultsKarsiTakim(File evalFile) {

		// read file , get total number of tp,fp,tn,fn, prec,recall,f1
		Double totalTp = 0.0;
		Double totalFp = 0.0;
		Double totalTn = 0.0;
		Double totalFn = 0.0;
		
		try{
			// Open the file
			FileInputStream fstream = new FileInputStream(evalFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			//Read File Line By Line until the index of target
			String strLine = null;
			while ((strLine = br.readLine()) != null)   // read info
			{
				// read related line, parse it and return output
				// Target \t TP,FP,TN,FN,Precision,Recall,F1Measure
				//E.g. [triamcinolone]	3.0, 2.0, 0.0, 9.0, 0.6, 0.25, 0.35294117647058826
				String[] splittedEvalResult = strLine.split("\t");
				String targetDrug = splittedEvalResult[0];
				String evalResultStr = splittedEvalResult[1];
				
				String[] splitted = evalResultStr.split(",");

				Double tp = Double.valueOf(splitted[0].trim());
				Double fp = Double.valueOf(splitted[1].trim());
				Double tn = Double.valueOf(splitted[2].trim());
				Double fn = Double.valueOf(splitted[3].trim());
				
				totalTp += tp;
				totalFp += fp;
				totalTn += tn;
				totalFn += fn;
				
			}

			// calculate evalRes
			EvaluationResult overallEvalRes = new EvaluationResult(totalTp, totalFp, totalTn, totalFn);
			

			// printResults
			String resultPath = evalResultFolderStr + "//OverallResult.csv";
			printer.printOverallEvalResult(resultPath, overallEvalRes);


		} catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void evaluateKarsiTakim(String resultFolderStr,
			Map<Item, List<Rating>> goldenDrugDiseaseMap) {
		// get files in result folder
		try{

			File resFolder = new File(resultFolderStr);
			File[] listOfFiles = resFolder.listFiles(); 

			for(File resFile:listOfFiles){
				String name = resFile.getName();

				// there can be some other files, just use rec output files (starts with recItem)
				if(name.startsWith("karsiTakim")){
					// run eval analysis
					// read created output : <targetDrug, List<recommendedDisease>>
					HashMap<Item, HashSet<Item>> resultData = readResultKarsiTakim(resFile);
					System.out.println("Karsi takim recommended #drug: " + resultData.keySet().size());

					// evaluate results
					HashMap<Item, EvaluationResult> evalResultMap = 
							evaluateResults(goldenDrugDiseaseMap, resultData);

					// printResults
					String evalResultPath = evalResultFolderStr+"evalResult"
							+ ".csv";
					printer.printEvalResult(evalResultPath, evalResultMap);

				}
			}

		} catch(Exception e){
			e.printStackTrace();
		}

	}


	private static void evaluate(String resultFolderStr,
			Map<Item, List<Rating>> goldenDrugDiseaseMap) {
		// get files in result folder
		try{

			File resFolder = new File(resultFolderStr);
			File[] listOfFiles = resFolder.listFiles(); 

			for(File resFile:listOfFiles){
				String name = resFile.getName();

				// there can be some other files, just use rec output files (starts with recItem)
				if(name.startsWith("recItems")){
					// decide on parameters
					DrugRecommenderParameters params = getParams(name);

					// run eval analysis
					runEvalAnalysis(goldenDrugDiseaseMap, resFile, params);

				}
			}

		} catch(Exception e){
			e.printStackTrace();
		}

	}

	private static DrugRecommenderParameters getParams(String name) {

		//E.g. recItems,DrugRecommenderParameters [numberOfSimilarItems=-1, 
		//			outputListSize=5, threshold=-1.0, prefferedMoType=ONLYDOMINATES, 
		//			outlistType=FIXEDLENGTH, itemSelectionType=SUM]
		String extractedParamString1 = name.replace(".tsv","").
				replace("recItems,DrugRecommenderParameters", "").
				replace("[", "").replace("]", "");
		String extractedParamString = extractedParamString1.replace(".csv","").
				replace("evalResult,DrugRecommenderParameters", "").
				replace("[", "").replace("]", "");

		String[] splitted = extractedParamString.split(",");

		String numberOfSimilarDrugsStr = splitted[0].replace("numberOfSimilarItems=", "").trim();
		Integer numberOfSimilarDrugs = Integer.valueOf(numberOfSimilarDrugsStr);

		String outputListSizeStr = splitted[1].replace("outputListSize=", "").trim();
		Integer outputListSize = Integer.valueOf(outputListSizeStr);

		String thresholdStr = splitted[2].replace("threshold=", "").trim();
		Double threshold = Double.valueOf(thresholdStr);

		MOType moType = DrugRecommenderParameters.moTypeDefault;
		ISType isType = DrugRecommenderParameters.isTypeDefault;
		OutlistType outlistType = DrugRecommenderParameters.outlistTypeDefault;

		String moTypeStr = splitted[3].replace("prefferedMoType=", "").trim();
		if(moTypeStr.equals(DrugRecommenderParameters.moTypeDefault.toString()) == false){
			moType = MOType.valueOf(moTypeStr);
		}

		String outlistTypeStr = splitted[4].replace("outlistType=", "").trim();
		if(outlistTypeStr.equals(DrugRecommenderParameters.outlistTypeDefault.toString()) == false){
			outlistType = OutlistType.valueOf(outlistTypeStr);
		}
		String isTypeStr = splitted[5].replace("itemSelectionType=", "").trim();
		if(isTypeStr.equals(DrugRecommenderParameters.isTypeDefault.toString()) == false){
			isType = ISType.valueOf(isTypeStr);
		}

		DrugRecommenderParameters params = new DrugRecommenderParameters(numberOfSimilarDrugs, 
				outputListSize, threshold, moType, outlistType, isType);
		return params;
	}

	private static void runEvalAnalysis(Map<Item, List<Rating>> goldenDrugDiseaseMap,
			File resFile, DrugRecommenderParameters params) {
		// read created output : <targetDrug, List<recommendedDisease>>
		HashMap<Item, HashSet<Item>> resultData = readResult(resFile);

		// evaluate results
		HashMap<Item, EvaluationResult> evalResultMap = 
				evaluateResults(goldenDrugDiseaseMap, resultData);

		// printResults
		String evalResultPath = evalResultFolderStr+"evalResult,"
				+ params.toString()
				+ ".csv";
		printer.printEvalResult(evalResultPath, evalResultMap);
	}



	private static HashMap<Item, EvaluationResult> evaluateResults(
			Map<Item, List<Rating>> goldenDrugDiseaseMap,
			HashMap<Item, HashSet<Item>> resultData) {

		HashMap<Item, EvaluationResult> evalResultMap = 
				new HashMap<Item, EvaluationResult>();
		
		// loop for all drug-disease in goldenData
		int total = 0;
		for(Entry<Item, List<Rating>> e: goldenDrugDiseaseMap.entrySet()){
			Item targetDrug = e.getKey();
			List<Rating> goldenDiseaseList = e.getValue();
			System.out.println(goldenDiseaseList.size() + " " + targetDrug.getNames());
			total += goldenDiseaseList.size();
			
			// control if targetDrug exists on resultData
			if(resultData.get(targetDrug) == null){
				// no recommendation for the targetDrug
				//System.err.println("No recommendation made for targetDrug " + targetDrug.toString());
				// add empty recommendation - to include to evalution
				EvaluationResult evalResult = compare(goldenDiseaseList, new HashSet<Item>());
				evalResultMap.put(targetDrug, evalResult);
			} else{
				HashSet<Item> resRecommendedDiseaseSet = resultData.get(targetDrug);
				EvaluationResult evalResult = compare(goldenDiseaseList, resRecommendedDiseaseSet);
				evalResultMap.put(targetDrug, evalResult);
			}
		}
		
		System.out.println(total);
		return evalResultMap;
	}

	private static EvaluationResult compare(
			List<Rating> goldenDiseaseList,
			HashSet<Item> resRecommendedDiseaseSet) {
		Double tp = 0.0; // predicted true, actually true
		Double fp = 0.0; // predicted true, actually false
		Double tn = 0.0; // predicted false, actually false
		Double fn = 0.0; // predicted false, actually true

		for(Item predictedDisease: resRecommendedDiseaseSet){
			if(doesContain(goldenDiseaseList, predictedDisease)){
				tp++;
			} else {
				fp++;
			}
		}

		fn =  goldenDiseaseList.size() - tp;
		
		//(TODO) NOTE I didn't calculate the tn

		EvaluationResult evalResult = new EvaluationResult(tp, fp, tn, fn);
		return evalResult;
	}

	/**
	 * 
	 * @param goldenDiseaseList: list of rating: recommenderDrug, recommendedDisease, ratingScore
	 * @param predictedDisease: predicted Disease
	 * @return
	 */
	private static boolean doesContain(List<Rating> goldenDiseaseList, Item predictedDisease) {
		boolean retVal = false;

		for(Rating rating:goldenDiseaseList){
			if(rating.getRecommendedItem().equals(predictedDisease)){
				retVal = true;
				break;
			}
		}

		return retVal;
	}


	/**
	 * 
	 * @param resFile
	 * @return <targetDrug, Set<recommendedDisease>>
	 */
	private static HashMap<Item, HashSet<Item>> readResult(File resFile) {
		// read file 
		HashMap<Item, HashSet<Item>> resultData = 
				new HashMap<Item, HashSet<Item>>();
		try{
			// Open the file
			FileInputStream fstream = new FileInputStream(resFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			//Read File Line By Line until the index of target
			String strLine = null;
			while ((strLine = br.readLine()) != null)   // read info
			{
				// TargetDrug \t recDisease \t recDisease...
				// E.g. [aca]	[malaria..falciparum]	[malaria..vivax] 
				// read related line, parse it and return output
				String[] splitted = strLine.split("\t");

				// splitted[0] may contain a list of names of the same drug
				String targetDrugStrList = splitted[0].replace("]", "").
						replace("[", "").trim().toLowerCase();
				String[] splittedTargetDrugStrList = targetDrugStrList.split(",");
				String targetDrugStr = splittedTargetDrugStrList[0].trim().toLowerCase();
				Item targetDrug = Utils.getItem(allDrugs, targetDrugStr);

				if(targetDrug != null){
					for(int i=1; i<splitted.length; i++){
						// splitted[i] may contain a list of names of the same disease
						String recommendedDiseaseStrList = splitted[i].replace("]", "").
								replace("[", "").trim().toLowerCase();
						String[] splittedRecommendedDiseaseStrList = recommendedDiseaseStrList.split(",");
						String recommendedDiseaseStr = splittedRecommendedDiseaseStrList[0].trim().toLowerCase();
						Item recommendedDisease = Utils.getItem(allDiseases, recommendedDiseaseStr);

						if(recommendedDisease != null){
							// control if target drug exists in the resultData, and add disease 
							HashSet<Item> recommendedSet = resultData.get(targetDrug);
							if(recommendedSet == null){
								// first time of targetDrug
								recommendedSet = new HashSet<Item>();
								recommendedSet.add(recommendedDisease);
								resultData.put(targetDrug, recommendedSet);
							} else {
								// targetGene is already seen
								recommendedSet.add(recommendedDisease);
								resultData.put(targetDrug, recommendedSet);
							}
						} else{
							System.err.println("recommendedDisease  is null?? recommendedDiseaseStr: " + recommendedDiseaseStr);
						}
					}			
				} else {
					System.err.println("Target drug is null?? targetDrugStr: " + targetDrugStr);
				}
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return resultData;
	}
	
	
	/**
	 * 
	 * @param resFile
	 * @return <targetDrug, Set<recommendedDisease>>
	 */
	private static HashMap<Item, HashSet<Item>> readResultKarsiTakim(File resFile) {
		// read file 
		HashMap<Item, HashSet<Item>> resultData = 
				new HashMap<Item, HashSet<Item>>();
		try{
			// Open the file
			FileInputStream fstream = new FileInputStream(resFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			//Read File Line By Line until the index of target
			String strLine = null;
			while ((strLine = br.readLine()) != null)   // read info
			{
				// TargetDrug , recDisease 
				// read related line, parse it and return output
				String[] splitted = strLine.split(",");
				String targetDrugStr = splitted[0].trim().toLowerCase();
				Item targetDrug = Utils.getItem(allDrugs, targetDrugStr);
				
				String recommendedDiseaseStr = splitted[1].trim().toLowerCase();
				Item recommendedDisease = Utils.getItem(allDiseases, recommendedDiseaseStr);
				
				if(targetDrug != null && recommendedDisease != null) {
					
					// control if target drug exists in the resultData, and add disease 
					HashSet<Item> recommendedSet = resultData.get(targetDrug);
					if(recommendedSet == null){
						// first time of targetDrug
						recommendedSet = new HashSet<Item>();
						recommendedSet.add(recommendedDisease);
						resultData.put(targetDrug, recommendedSet);
					} else {
						// targetGene is already seen
						recommendedSet.add(recommendedDisease);
						resultData.put(targetDrug, recommendedSet);
					}
				}

			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return resultData;
	}
}
