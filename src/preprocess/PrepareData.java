package preprocess;

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


public class PrepareData {

	static String resultFolderPath = ".//data//prepData//";
	protected static String drugDiseasePath = ".//data//drug_disease.tsv";

	static Printer printer = new FilePrinter(false);

	static double ratio = 0.20;
	
	static String[] diseaseNames;

	public static void main(String[] args) {

		// read golden nw data : drugname --> list of diseasenames
		Map<String, List<String>> goldenDrugDiseaseMap = readKnownDrugDiseaseMap(drugDiseasePath);

		// create test and train data
		Map<String, List<String>> testData = new HashMap<String, List<String>>();
		Map<String, List<String>> trainData = new HashMap<String, List<String>>();
		createTestTrainData(goldenDrugDiseaseMap, testData, trainData);

		// convert test train data to binary vector
		System.out.println("trainBinaryMatrix: " );
		Map<String, Integer[]> trainBinaryMatrix = createBinaryMatrix(diseaseNames, trainData);
		System.out.println("testBinaryMatrix: " );
		Map<String, Integer[]> testBinaryMatrix = createBinaryMatrix(diseaseNames, testData);
		
		// write the train/test data to output
		String trainPath = resultFolderPath + "//train.tsv";
		printer.printDataTsv(trainPath, diseaseNames, trainBinaryMatrix);

		String testPath = resultFolderPath + "//test.tsv";
		printer.printDataTsv(testPath, diseaseNames, testBinaryMatrix);

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

	private static void createTestTrainData(Map<String, List<String>> goldenDrugDiseaseMap,
			Map<String, List<String>> testData, Map<String, List<String>> trainData) {
		// find min count an item can have (e.g. 1/0.20=5) to be selected for the test set creation
		int minCount = (int) (1.0/ratio);

		for(Entry<String, List<String>> entry: goldenDrugDiseaseMap.entrySet()){
			String drug = entry.getKey();
			List<String> diseaseList = entry.getValue();

			if(diseaseList.size() >= minCount){
				// the drug has at least minCount elements, so put ratio of it to test data and rest to train data
				List<String> testRatingList = new ArrayList<String>();
				List<String> trainRatingList = new ArrayList<String>();

				cretaeTestTrainRatingList(diseaseList, testRatingList, trainRatingList);

				testData.put(drug, testRatingList);
				trainData.put(drug, trainRatingList);
			} else {
				// the drug has less than minCount elements, so directly put it to train set
				trainData.put(drug, diseaseList);
			}

		}

	}

	private static void cretaeTestTrainRatingList(List<String> diseaseList,
			List<String> testRatingList,
			List<String> trainRatingList) {

		// randomly select the indices of ratings to be put on test set
		int count = (int) (diseaseList.size() * ratio);
		Set<Integer> selectedIndices = new HashSet<Integer>();
		while(selectedIndices.size() < count){
			Integer randomIndex = (int)(Math.random() * diseaseList.size());
			if(selectedIndices.contains(randomIndex) == false){
				selectedIndices.add(randomIndex);
			}
		}

		// put the elements in the selectedIndices to the test set, 
		// and the rest to the training set
		for(int i=0 ; i< diseaseList.size(); i++){
			if(selectedIndices.contains(i)){
				testRatingList.add(diseaseList.get(i));
			} else{
				trainRatingList.add(diseaseList.get(i));
			}

		}
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
