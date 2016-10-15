package util;

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
import java.util.Set;

import drugRelated.Item;
import recRelated.Rating;

public class ItemReader {


	/**
	 * filePath contains items in each line
	 * Each line is composed of names that belong to a single item, (tab separated)
	 * @param filePath
	 * @return
	 */
	public static Set<Item> readAllItems(String filePath) {
		Set<Item> allItems = new HashSet<>();
		// read file 
		try{
			// Open the file
			FileInputStream fstream = new FileInputStream(filePath);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			//Read File Line By Line 
			String strLine = null;
			while ((strLine = br.readLine()) != null) {
				String[] splitted = strLine.split("\t");
				Set<String> names = new HashSet<>();
				for(String split:splitted){
					String name = split.toLowerCase().trim();
					if(name.length() > 0){
						names.add(name);
					}
				}

				Integer id = allItems.size();
				Item newItem = new Item(id, names);
				allItems.add(newItem);
			}

		} catch(Exception e){
			e.printStackTrace();
		}
		return allItems;
	}


	public static Map<Item, List<Rating>> readKnownDrugDiseaseMap(String path,
			Set<Item> allDrugs, 
			Set<Item> allDiseases) {
		Map<Item, List<Rating>> drugDiseaseMap = new HashMap<Item, List<Rating>>();

		try {
			// read file
			// Open the file
			FileInputStream fstream = new FileInputStream(path);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			// Read 1 line for header (disease names)
			String header = br.readLine();
			String[] diseaseNames = header.split("\t");//TODO control if 1st one is empty!!

			//Read File Line By Line until the index of target
			String strLine = null;

			while ((strLine = br.readLine()) != null)   // read info
			{
				String[] splitted = strLine.split("\t");
				String drugName=splitted[0].toLowerCase().trim();
				Item drug = Utils.getItem(allDrugs, drugName);
				if(drug == null){
					//System.err.println("Drug named " + drugName + " does not exist in allDrugs list");
				} else {
					for(int i=1; i< splitted.length; i++){
						Double ratingScore = Double.valueOf(splitted[i]);
						if(ratingScore > 0.0 ){
							// drug is related to the item
							String diseaseName = diseaseNames[i].toLowerCase().trim();
							Item disease = Utils.getItem(allDiseases, diseaseName);
							if(disease == null){
								//System.err.println("Disease named " + diseaseName + " does not exist in allDiseases list");
							} else{
								// add disease to the map
								addToMap(drugDiseaseMap, drug, disease, ratingScore);
							}
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

	private static void addToMap(Map<Item, List<Rating>> drugDiseaseMap, Item drug, 
			Item disease, Double ratingScore) {
		if(drugDiseaseMap.get(drug) != null){
			// add disease to the list
			List<Rating> diseaseList = drugDiseaseMap.get(drug);
			Rating rating = new Rating(drug, disease, ratingScore);
			diseaseList.add(rating);
			drugDiseaseMap.put(drug, diseaseList);
		} else{
			// create list and add disease to the list
			List<Rating> diseaseList = new ArrayList<>();
			Rating rating = new Rating(drug, disease, ratingScore);
			diseaseList.add(rating);
			drugDiseaseMap.put(drug, diseaseList);
		}
	}


	public static Set<Item> readTargets(String path, Set<Item> allDrugs) {
		Set<Item> drugSet = new HashSet<>();

		try {
			// read file
			// Open the file
			FileInputStream fstream = new FileInputStream(path);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			// Read 1 line for header (disease names)
			String header = br.readLine();
			String[] diseaseNames = header.split("\t");//TODO control if 1st one is empty!!

			//Read File Line By Line until the index of target
			String strLine = null;

			while ((strLine = br.readLine()) != null)   // read info
			{
				String[] splitted = strLine.split("\t");
				String drugName=splitted[0].toLowerCase().trim();
				Item drug = Utils.getItem(allDrugs, drugName);
				if(drug == null){
					//System.err.println("Drug named " + drugName + " does not exist in allDrugs list" );
				} else {
					drugSet.add(drug);
				}
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


		return drugSet;

	}
}
