package preprocess;

import java.awt.event.ItemEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import drugRelated.Item;
import recRelated.Rating;
import util.FilePrinter;
import util.ItemReader;
import util.Printer;
import util.Utils;


public class PrepareDrugDiseaseSubset {

	static String resultFolderPath = ".//data//prepData//";
	protected static String allDrugsPath = ".//data//allDrugs.txt";
	protected static String drugDiseasePath = ".//data//drug_disease.tsv";

	static Printer printer = new FilePrinter(false);
	
	static String[] diseaseNames;

	public static void main(String[] args) {

		// read golden nw data : drugname --> list of diseasenames
		Map<String, List<String>> goldenDrugDiseaseMap = readKnownDrugDiseaseMap(drugDiseasePath);

		// read drugs existing in the data
		Set<Item> allDrugs = ItemReader.readAllItems(allDrugsPath);
		
		// create the subset
		Map<String, List<String>> subData = new HashMap<String, List<String>>();
		createSubData(goldenDrugDiseaseMap, allDrugs, subData);
		
		// convert test train data to binary vector
		Map<String, Integer[]> subsetBinaryMatrix = createBinaryMatrix(diseaseNames, subData);
		
		// write subset data to output
		String testPath = resultFolderPath + "//subData_drug_disease.tsv";
		printer.printDataTsv(testPath, diseaseNames, subsetBinaryMatrix);

	}

	private static void createSubData(Map<String, List<String>> goldenDrugDiseaseMap, 
			Set<Item> allDrugs,
			Map<String, List<String>> subData) {
		

		for(Entry<String, List<String>> entry: goldenDrugDiseaseMap.entrySet()){
			String drug = entry.getKey();
			List<String> diseaseList = entry.getValue();

			if(Utils.doesContain(allDrugs, drug.toLowerCase()) == true){
				subData.put(drug, diseaseList);
			} else{
				System.out.println("drug: " + drug);
			}

		}
		
	}

	private static Map<String, Integer[]> createBinaryMatrix(String[] diseaseNames,
			Map<String, List<String>> drugDiseaseMap) {
		//createMatrix --> drug-> diseaseExist/not
		Map<String, Integer[]> matrix = new HashMap<String, Integer[]>();
		for(Entry<String, List<String>> entry: drugDiseaseMap.entrySet()){
			// get name
			String drugName = entry.getKey();

			// create binary array
			List<String> relatedDiseaseNames = entry.getValue();
			if(relatedDiseaseNames.size() == 0){
				System.out.println("debug");
			}
			Integer[] vector = createVector(diseaseNames, relatedDiseaseNames);

			// put to the matrix
			matrix.put(drugName, vector);
		}

		return matrix;
	}

	private static Integer[] createVector(String[] diseaseNames, List<String> relatedDiseaseNames) {
		Integer[] vector = new Integer[diseaseNames.length];
		
		int oneCount = 0;
		for(int i = 0; i< diseaseNames.length; i++){
			String diseaseName = diseaseNames[i];
		
			if(relatedDiseaseNames.contains(diseaseName)){
				vector[i] = 1;
				oneCount++;
			} else{
				vector[i] = 0;
			}
		}


		System.out.println("oneCount: " + oneCount);

		
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
			String strLine = null;

			while ((strLine = br.readLine()) != null)   // read info
			{
				String[] splitted = strLine.split("\t");
				String drugName=splitted[0].trim();

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
