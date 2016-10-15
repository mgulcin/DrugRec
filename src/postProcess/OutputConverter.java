package postProcess;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import method.DrugRecommenderParameters;
import drugRelated.Item;
import eval.EvaluationResult;
import util.FilePrinter;
import util.ItemReader;
import util.Printer;
import util.Utils;

public class OutputConverter {
	static String resultFolderPath = ".//selectedOutputConverted//";
	static String inputFolderPath = ".//selectedOutput//";
	
	protected static String allDrugsPath = ".//data//allDrugs.txt";
	protected static String allDiseasesPath = ".//data//allDiseases.txt";
	protected static String drugDiseasePath = ".//data//prepData//subData_drug_disease.tsv";

	static Printer printer = new FilePrinter(false);

	// read from drugDiseasePath, used to reconstruct matrix
	static String[] diseaseNames;
	static ArrayList<String> drugNames;

	// read from file (contains name maps)
	static Set<Item> allDrugs;
	static Set<Item> allDiseases;
	
	public static void main(String[] args) {

		// read allDrugs name list
		allDrugs = ItemReader.readAllItems(allDrugsPath);
		allDiseases = ItemReader.readAllItems(allDiseasesPath);
		
		// read golden nw data : drugname --> list of diseasenames
		Map<String, List<String>> goldenDrugDiseaseMap = readKnownDrugDiseaseMap(drugDiseasePath);

		// convert output
		convertOutput(inputFolderPath);

	}

	private static void convertOutput(String inputFolderPath) {
		// get files in result folder
		try{

			File resFolder = new File(inputFolderPath);
			File[] listOfFiles = resFolder.listFiles(); 

			for(File resFile:listOfFiles){
				String name = resFile.getName();

				// there can be some other files, just use rec output files (starts with recItem)
				if(name.startsWith("recItems")){
					

					Map<String, Integer[]> outputBinaryMatrix = createBinaryMatrix(diseaseNames, resFile);
					
					// write outputConverted
					String outputConvertedPath = resultFolderPath + "//" + name;
					printer.printDataTsv(outputConvertedPath, diseaseNames, outputBinaryMatrix);


				}
			}

		} catch(Exception e){
			e.printStackTrace();
		}

	}
	
	private static Map<String, Integer[]> createBinaryMatrix(String[] diseaseNames,
			File resFile) {
		//createMatrix --> drug-> diseaseExist/not
		Map<String, Integer[]> matrix = new HashMap<String, Integer[]>();
		
		// read created output : <targetDrug, List<recommendedDisease>>
		HashMap<Item, HashSet<Item>> resultData = readResult(resFile);
		
		System.out.println("resData size" + resultData.size());
		for(String drugName: drugNames){
				if(drugName.equals("")){
					// TODO why is there an empty drug, is it related to last comma on csv file
					//System.out.println("debug");
					continue;
				}
				Item drugItem = findDrugItem(resultData.keySet(), drugName);
				HashSet<Item> diseaseSet = resultData.get(drugItem);


				// create binary array
				List<String> relatedDiseaseNames = createDiseaseNameList(diseaseSet);
				Integer[] vector = createVector(diseaseNames, relatedDiseaseNames);

				// put to the matrix
				matrix.put(drugName, vector);
			
		}

		return matrix;
	}
	
	private static Item findDrugItem(Set<Item> keySet, String drugName) {
		Item retVal = null;
		
		for(Item item: keySet){
			if(doesContain(item.getNames(), drugName)){
				retVal = item;
				break;
			}
		}
		
		
		return retVal;
	}

	/**
	 * return drug name as written in the drug-disease map (golden data)
	 * @param drugItem
	 * @return
	 */
	private static String getDrugName(Item drugItem) {
		String retVal = null;
		for(String drugName: drugNames){
			boolean isSameDrugName = doesContain(drugItem.getNames(), drugName);
			if(isSameDrugName == true){
				retVal = drugName;
				break;
			}
		}

		return retVal;
	}
	
	private static boolean doesContain(Collection<String> relatedDiseaseNames,
			String name) {
		boolean retVal = false;
		for(String n: relatedDiseaseNames){
			if(n.toLowerCase().trim().equals(name.toLowerCase().trim())){
				retVal = true;
				break;
			}
		}
		return retVal;
	}

	private static List<String> createDiseaseNameList(HashSet<Item> diseaseSet) {
		List<String> retDiseaseNames = new ArrayList<String>();
		
		Set<String> diseaseNamesSet = new HashSet<String>(Arrays.asList(diseaseNames));
		if(diseaseSet == null || diseaseSet.size() == 0){
			// do nothing, nothing to find
			//System.out.println("debug");
		} else{
			for(Item diseaseItem: diseaseSet){
				Set<String> diseaseNameSet = diseaseItem.getNames();
				
				for(String diseaseName: diseaseNameSet){
					if(doesContain(diseaseNamesSet,diseaseName)){
						retDiseaseNames.add(diseaseName);
						break;
					}
				}
			}
		}
		
		return retDiseaseNames;
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
					
					// no predictions was made
					if(resultData.get(targetDrug) == null){
						HashSet<Item> recommendedSet = new HashSet<Item>();
						resultData.put(targetDrug, recommendedSet);
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

	
	private static Integer[] createVector(String[] diseaseNames, List<String> relatedDiseaseNames) {
		Integer[] vector = new Integer[diseaseNames.length];
		
		for(int i = 0; i< diseaseNames.length; i++){
			String diseaseName = diseaseNames[i];
		
			if(doesContain(relatedDiseaseNames, diseaseName)){
				vector[i] = 1;
			} else{
				vector[i] = 0;
			}
		}

		
		return vector;
	}

	public static Map<String, List<String>> readKnownDrugDiseaseMap(String path) {
		Map<String, List<String>> drugDiseaseMap = new HashMap<>();

		try {
			// read file
			// Open the file
			FileInputStream fstream = new FileInputStream(path);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			// Read 1 line for header (disease names)
			String header = br.readLine();
			String[] diseaseNamesTemp = header.split("\t");//TODO control if 1st one is empty!!
			diseaseNames = new String[diseaseNamesTemp.length-1];
			for(int i =1; i< diseaseNamesTemp.length; i++){
				diseaseNames[i-1] = diseaseNamesTemp[i];
			}
			
			//Read File Line By Line until the index of target
			drugNames = new ArrayList<String>();
			String strLine = null;

			while ((strLine = br.readLine()) != null)   // read info
			{
				String[] splitted = strLine.split("\t");
				String drugName=splitted[0].trim();
				
				drugNames.add(drugName);

				for(int i=1; i< splitted.length; i++){
					Double ratingScore = Double.valueOf(splitted[i]);
					if(ratingScore > 0.0 ){
						// drug is related to the item
						String diseaseName = diseaseNamesTemp[i].trim();

						// add disease to the map
						if(drugDiseaseMap.get(drugName) != null){
							// add disease to the list
							List<String> diseaseList = drugDiseaseMap.get(drugName);
							diseaseList.add(diseaseName);
							drugDiseaseMap.put(drugName, diseaseList);
						} else{
							// create list and add disease to the list
							List<String> diseaseList = new ArrayList<>();
							diseaseList.add(diseaseName);
							drugDiseaseMap.put(drugName, diseaseList);
						}

					}
				}

			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return drugDiseaseMap;
	}
}
