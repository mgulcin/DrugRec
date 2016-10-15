package recRelated;

import java.util.HashMap;

import drugRelated.Item;

public class Similarity {
	private Item item;
	private HashMap<SimType, Double> similarityMap; // <simType,simVal> 

	// sim types
	public enum SimType {STRUCTURE, PROTEIN, SIDER, DRUG}

	public Similarity(Item item, HashMap<SimType, Double> similarityMap) {
		super();
		this.item = item;
		this.similarityMap = similarityMap;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public HashMap<SimType, Double> getSimilarityMap() {
		return similarityMap;
	}

	public void setSimilarityMap(HashMap<SimType, Double> similarityMap) {
		this.similarityMap = similarityMap;
	}
    
	public static  String decideFileName(SimType field, String folderName) {
		String retVal ="";
		switch(field){
		case PROTEIN: 
			retVal = "drug_protein_CosSim.tsv"; // Cosine > Jaccard
			//retVal = "drug_protein_SWSim.tsv"; 
			break;
		case STRUCTURE: 
			retVal = "drug_structure_CosSim.tsv";
			//retVal = "drug_structure_JaccardSim.tsv";  // Jaccard > Cosine
			break;
		case SIDER: 
			retVal = "drug_sider_CosSim.tsv"; 
			//retVal = "drug_sider_JaccardSim.tsv";  // Jaccard > Cosine
			break;
		case DRUG: 
			retVal = "drug_drug_JaccardSim.tsv"; // Does Not work!!
			break;
		default:
			System.out.println("Wrong similarity type. The file name could not be decided.");
			break;
		}
		return retVal;
	}

	@Override
	public String toString() {
		return "Similarity [item=" + item + ", similarityMap=" + similarityMap + "]";
	}
	
	
}
