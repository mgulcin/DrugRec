package method;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import recRelated.MOBasedSimilarityCalculator;
import recRelated.Rating;
import recRelated.Recommendation;
import recRelated.RecommendationComparator;
import recRelated.Similarity;
import recRelated.Similarity.SimType;
import util.Printer;
import util.Utils;

import java.util.PriorityQueue;
import java.util.Set;

import drugRelated.DrugInfo;
import drugRelated.Item;
import main.MainDrugRec;
import method.DrugRecommenderParameters.MOType;
import method.DrugRecommenderParameters.OutlistType;


public class DrugRecommender {

	//parameters used in calculations
	protected String inputPath;
	protected String folderName;


	protected DrugRecommenderParameters parameters;

	// used for to get weights!!! Re-design here
	HashMap<Item,HashMap<SimType, Double>> simValByDrug;

	
	// methods
	public DrugRecommender(String inputPath, String outputPath,
			DrugRecommenderParameters parameters) {
		super();
		this.inputPath = inputPath;
		this.folderName ="";
		this.parameters = parameters;
	}

	public DrugRecommender(String inputPath, String outputPath) {
		super();
		this.inputPath = inputPath;
		this.folderName ="";
		this.parameters = null;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public DrugRecommenderParameters getParameters() {
		return parameters;
	}


	public void setParameters(DrugRecommenderParameters parameters) {
		this.parameters = parameters;
	}


	// 
	public ArrayList<Recommendation> recommend(Item target, 
			ArrayList<SimType> simFieldList, Map<Item, List<Rating>> drugDiseaseMap,
			Set<Item> allDiseases)
					throws IOException {
		// find item-recScore by collobarative filtering

		
		// 1) Find k-many similar Drugs 
		ArrayList<Item> similarDrugs = getMostSimilarDrugs(target, 
				parameters.getNumberOfSimilarItems(), simFieldList);
		
		// control if target is listed on similarDrugs!!
		if(similarDrugs.contains(target)){
			System.err.println("Target: " + target.toString() + "is listed on similarDrugs, "
					+ "MOType: " + parameters.getPrefferedMoType());
		}
				

		//		// 2) Print most similar Drugs to each target Drug
		//		String simDrugsOutPath = outputPath + "simDrugs_"
		//				+ prefferedMoType.toString() + ".csv";
		//		printer.printMostSimilars(simDrugsOutPath, targetDrug,similarDrugs);

		// 3) Find recommendations - sorted by score!!
		PriorityQueue<Recommendation> rec = findRecommendations(target, 
				similarDrugs, drugDiseaseMap);

		// 4) Return best k recommendation as a result
		ArrayList<Recommendation> resultRecs = getBestKRecommendations(target, 
				rec, parameters.getOutputListSize());

		/*
		// 5) If not filled outputList size, guess random!!
		switch(parameters.getOutlistType()){
		case FIXEDLENGTH:{
			predictRandomlyFixedLength(resultRecs, target, allDiseases, parameters.getOutputListSize());
		}
		break;
		}
*/

		// return 
		return resultRecs;
		
	}


	private void predictRandomlyFixedLength(ArrayList<Recommendation> resultRecs, 
			Item target, Set<Item> allDiseases,
			Integer outputListSize) {
		int allDiseaseSize = allDiseases.size();
		List<Item> allDiseaseList = new ArrayList<Item>(allDiseases);
		
		while(resultRecs.size() < outputListSize){
			//guess random 
			Integer randomIndex = (int)(Math.random() * allDiseaseSize);
			Item disease = allDiseaseList.get(randomIndex);
			
			if(isRecommended(resultRecs, disease) == false){
				Recommendation rec = new Recommendation(disease, 1.0);
				resultRecs.add(rec);
			}
		}
		
	}

	private boolean isRecommended(ArrayList<Recommendation> resultRecs, Item disease) {
		boolean retVal = false;
		for(Recommendation rec: resultRecs){
			if(rec.getRecommendedItem().equals(disease)){
				retVal = true;
				break;
			}
		}
		return retVal;
	}

	protected ArrayList<Recommendation> getBestKRecommendations(
			Item target, PriorityQueue<Recommendation> rec,
			Integer outputListSize) {
		ArrayList<Recommendation> resultMap = null;
		// Return best k recommendation as a result
		switch(parameters.getOutlistType()){
		case FIXEDLENGTH:
		{
			// Return best k recommendation as a result
			resultMap = new ArrayList<Recommendation>();
			while(resultMap.size() < outputListSize){
				Recommendation r = rec.poll();

				if(r!=null){
					resultMap.add(r);	
				} else {
					// no element left in the queue
					break;
				}

			}
		}
		break;
		case THRESHOLDBASED:
		{
			resultMap = new ArrayList<Recommendation>();
			while(rec.size() > 0){
				Recommendation r = rec.poll();

				if(r!=null){
					if(r.getScore() > 0){
						resultMap.add(r);	
					}
				} else {
					// no element left in the queue
					break;
				}
			}
		}
		break;
		default: break;
		}


		return resultMap;
	}

	/**
	 * 
	 * @param target
	 * @param similarDrugs: neighbors
	 * @param drugDiseaseRatingList
	 * @return
	 * @throws IOException
	 */
	protected PriorityQueue<Recommendation> findRecommendations(Item target,
			ArrayList<Item> similarDrugs, Map<Item, List<Rating>> drugDiseaseRatingList) throws IOException {

		// 1) Get ratings from similar Drugs (rating, drugsWhoRecommendedThis )
		// for each recommended item(disease), create a list containing info of prob & recommender
		// recommended --> recommender + vals
		HashMap<Item, ArrayList<Rating>> allRecommendedItems= 
				combineRecommendations(target, similarDrugs, drugDiseaseRatingList);

		// 2) Calculate recommendation score for each item
		// // recommended --> rating
		HashMap<Item, Double> itemRecMap = 
				calculateDrugScores(target, allRecommendedItems);

		// 3) find items  & sort acc score
		PriorityQueue<Recommendation> rec = createRecs(itemRecMap);

		return rec;
	}

//	protected HashMap<String, ArrayList<Rating>> getRatings(SimType field) 
//			throws IOException {
//		HashMap<String, ArrayList<Rating>> ratingsMap = new HashMap<String, ArrayList<Rating>>();
//
//		// decide on fileName to use
//		String fileName = Similarity.decideFileName(field, folderName);
//
//		// set path
//		String path = inputPath + fileName;
//
//		// read file
//		// Open the file
//		FileInputStream fstream = new FileInputStream(path);
//		DataInputStream in = new DataInputStream(fstream);
//		BufferedReader br = new BufferedReader(new InputStreamReader(in));
//
//		// Read 1 line for header 
//		String header = br.readLine();
//		String[] splittedHeader = header.split("\t");
//
//		//Read File Line By Line until the index of target
//		String strLine = null;
//		while ((strLine = br.readLine()) != null)   // read info
//		{	
//			// read line, parse it and add to map
//			String[] splitted = strLine.split("\t");
//			String stdNameTarget = splitted[0].replace("\"", "");
//
//			String recommenderDrugName = splitted[0].replace("\"", "");
//			for(int i = 1; i < splitted.length; i++){
//				String recommendedDrugName = splittedHeader[i].replace("\"", "");
//				Double value = Double.valueOf(splitted[i]);
//
//				insertTo(ratingsMap, recommendedDrugName, recommenderDrugName, value);
//			}
//
//		}
//
//		return ratingsMap;
//	}
	
//	//TODO bunu nerde cagirmisti???
//	private void insertToRatingMap(
//			HashMap<Item, HashMap<SimType, ArrayList<Rating>>> simValByDrug,
//			SimType simType, Item drug, Double value) {
//		//map: (recommender-->recommended+vals)
//		// control if any entry exists for this Drug
//		ArrayList<Rating> ratingList = simValByDrug.get(drug);
//		if(ratingList == null){
//			// no such entry exists before
//			ratingList = new ArrayList<Rating>();
//			Rating rating = new Rating(drug, simType, value);
//			ratingList.add(rating);
//			simValByDrug.put(drug, ratingList);
//		} else {
//			// update entry
//			Rating rating = new Rating(drug, simType, value);
//			ratingList.add(rating);
//			simValByDrug.put(drug, ratingList);
//		}
//
//	}


	protected PriorityQueue<Recommendation> createRecs(
			HashMap<Item, Double> itemRecMap) {
		// for each item add score and create recommendation	
		PriorityQueue<Recommendation> recList = null;
		try{
			Comparator<Recommendation> recComp = new RecommendationComparator();
			int size = 1;
			if(parameters.getOutputListSize() != parameters.outputListSizeDefault){
				size = parameters.getOutputListSize();
			}
			recList = new PriorityQueue<Recommendation>(size, recComp);

			for(Entry<Item, Double> e:itemRecMap.entrySet()){
				Item recommendedItem = e.getKey();
				Double score = e.getValue();

				Recommendation rec = new Recommendation(recommendedItem, score);
				recList.add(rec);
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		return recList;
	}

	/**
	 * 
	 * @param target
	 * @param allRecommendedItems: recommended --> rating(recommender, recommended, ratingval)
	 * @return recommended -> recScore
	 */
	protected HashMap<Item, Double> calculateDrugScores(Item target,
			HashMap<Item, ArrayList<Rating>> allRecommendedItems) {
		// calculate average, for items
		HashMap<Item, Double> recScores = new HashMap<Item, Double>();

		// recommended --> rating
		for(Map.Entry<Item, ArrayList<Rating>> r:allRecommendedItems.entrySet()){
			Item recommendedItem = r.getKey();
			ArrayList<Rating> ratingList = r.getValue();

			Double recScore = findRecScore(recommendedItem, target, ratingList);
			recScores.put(recommendedItem, recScore);
		}

		return recScores;
	}

	/**
	 * 
	 * @param recommendedItem
	 * @param target
	 * @param ratingList: list of rating(recommender, recommended, ratingval)
	 * @return
	 */
	protected Double findRecScore(Item recommendedItem, Item target, 
			ArrayList<Rating> ratingList) {
		// use probVals as weights
		Double recScore = 0.0;

		switch(parameters.getItemSelectionType()){
		case SUM:
			recScore =findRecScoreSum(recommendedItem, target, ratingList);
			break;
		case WEIGHTEDSUM:
			recScore =findRecScoreWeightedSum(recommendedItem, target, ratingList);
			break;
		case AVG:
			recScore =findRecScoreAvg(recommendedItem, target, ratingList);
			break;
		case MAX:
			recScore =findRecScoreMax(recommendedItem, target, ratingList);
			break;
		case WEIGHTEDAVG: 
			recScore =findRecScoreWeightedAvg(recommendedItem, target, ratingList);
			break;
		default:
			System.out.println("Wrong isType");
			System.exit(-1);
			break;
		}

		return recScore;
	}

	private Double findRecScoreMax(Item recommendedItem, Item target,
			ArrayList<Rating> ratingList) {
		Double recScore = 0.0;

		for(Rating rating: ratingList){
			// get ratingscore
			Double ratingScore = rating.getRatingScore();

			// sum up avgBindingProbs of users
			if(parameters.getOutlistType() == OutlistType.THRESHOLDBASED){
				if(ratingScore >= parameters.getThreshold()){
					if(ratingScore > recScore){
						recScore = ratingScore;
					}
				}
			} else if(parameters.getOutlistType() == OutlistType.FIXEDLENGTH){
				if(ratingScore > recScore){
					recScore = ratingScore;
				}
			} else {
				System.out.println("Wrong outputListType");
				System.exit(-1);
			}

		}

		return recScore;
	}

	private Double findRecScoreAvg(Item recommendedItem, Item target,
			ArrayList<Rating> ratingList) {
		Double recScore = 0.0;

		for(Rating rating: ratingList){
			// get connection probVal
			Double ratingScore = rating.getRatingScore();

			// sum up avgBindingProbs of users
			if(parameters.getOutlistType() == OutlistType.THRESHOLDBASED){
				if(ratingScore >= parameters.getThreshold()){
					recScore += ratingScore;
				}
			} else if(parameters.getOutlistType() == OutlistType.FIXEDLENGTH){
				recScore += ratingScore;	
			} else {
				System.out.println("Wrong outputListType");
				System.exit(-1);
			}

		}
		// recscore is avg of avgBindingProbs of users
		recScore = recScore/ratingList.size();

		return recScore;
	}

	/**
	 * 
	 * @param recommendedItem
	 * @param target
	 * @param ratingList : Contains recommender, recommended and rating info. 
	 * 					   recommended in here and the first parameter are the same.
	 * @return
	 */
	private Double findRecScoreWeightedAvg(Item recommendedItem, 
			Item target,
			ArrayList<Rating> ratingList) {
		Double recScore = 0.0;

		Double totalWeight = 0.0;
		for(Rating rating: ratingList){
			// get ratingScore
			Double ratingScore = rating.getRatingScore();
			Item recommenderItem = rating.getRecommenderItem();

			// sum up ratingScores 
			if(parameters.getOutlistType() == OutlistType.THRESHOLDBASED){
				if(ratingScore >= parameters.getThreshold()){
					Double weight = getWeight(target, recommenderItem);
					totalWeight += weight;
					recScore += ratingScore * weight;
				}
			} else if(parameters.getOutlistType() == OutlistType.FIXEDLENGTH){
				Double weight =  getWeight(target, recommenderItem);
				totalWeight += weight;
				recScore += ratingScore * weight;
			} else {
				System.out.println("Wrong outputListType");
				System.exit(-1);
			}

		}
		// recscore is avg of avgBindingProbs of users
		recScore = recScore/totalWeight;

		return recScore;
	}
	
	/**
	 * Now, returning 1 -->should be different than 1!!!
	 * TODO Normally this should be dynamic, 
	 * and user should be able to decide on which measure to be used as weight
	 * 
	 * @param target: Who is given the recommendation at the end
	 * @param second: One of the selected neighbor 
	 * @return
	 */
	private Double getWeight(Item target, Item neighbor) {

//		Double weight = 1.0;
//		return weight;
		
	
		Double weight = null;
		HashMap<SimType, Double> simValMap = simValByDrug.get(neighbor);
		
		if(simValMap != null){
			Double totalWeight = 0.0;
			for(Entry<SimType, Double> e: simValMap.entrySet()){
				totalWeight += e.getValue();
			}
			weight = totalWeight / simValMap.size();
		}
		
		

		if(weight == null){
			System.out.println("An error found: No sim found between "
					+ "target "+ target+ " and " + neighbor);
			System.exit(-1);
		}
		if(weight.equals(0.0)){
			System.out.println("An error found(related to parto dom): sim is 0 between "
					+ "target "+ target+ " and " + neighbor);
			
		}

		return weight;
	}


	/**
	 * Now, we are getting the weights from connections among recommender-recommended,
	 * TODO Normally this should be dynamic, 
	 * and user should be able to decide on which measure to be used as weight
	 * 
	 * @param recommended
	 * @param target
	 * @param  list of rating(recommender, recommended, ratingval)
	 * @return
	 */
	private Double findRecScoreSum(Item recommendedItem, Item target,
			ArrayList<Rating> ratingList) {
		Double recScore = 0.0;
		for(Rating rating: ratingList){
			// get connection probVal
			Double ratingScore = rating.getRatingScore();

			// recscore is total of rating scores
			if(parameters.getOutlistType() == OutlistType.THRESHOLDBASED){
				if(ratingScore >= parameters.getThreshold()){
					recScore += ratingScore;
				}
			} else if(parameters.getOutlistType() == OutlistType.FIXEDLENGTH){
				recScore += ratingScore;	
			} else {
				System.out.println("Wrong outputListType");
				System.exit(-1);
			}

		}
		return recScore;
	}
	
	private Double findRecScoreWeightedSum(Item recommendedItem, Item target,
			ArrayList<Rating> ratingList) {
		Double recScore = 0.0;
		
		for(Rating rating: ratingList){
			// get rating score
			Double ratingScore = rating.getRatingScore();
			
			// sum up ratingScores 
			Item recommenderItem = rating.getRecommenderItem();
			if(parameters.getOutlistType() == OutlistType.THRESHOLDBASED){
				if(ratingScore >= parameters.getThreshold()){
					Double weight = getWeight(target, recommenderItem);
					recScore += ratingScore * weight;
				}
			} else if(parameters.getOutlistType() == OutlistType.FIXEDLENGTH){
				Double weight =  getWeight(target, recommenderItem);
				recScore += ratingScore * weight;
			} else {
				System.out.println("Wrong outputListType");
				System.exit(-1);
			}

		}
		
		return recScore;
	}
	

	
	/**
	 * 
	 * @param targetDrug: target
	 * @param similarDrugs: neighbors
	 * @param drugDiseaseRatingList: drug; list<diseases>
	 * @return recommended --> rating(recommender, recommended, ratingval)
	 */
	protected HashMap<Item, ArrayList<Rating>> combineRecommendations(
			Item target, ArrayList<Item> similarDrugs, 
			Map<Item, List<Rating>> drugDiseaseRatingList) {
		// combine recommender and recommended items
		// recommended --> recommender + vals
		HashMap<Item, ArrayList<Rating>> allRecommendedItems = 
				new HashMap<Item, ArrayList<Rating>>();

		for(Item simDrug: similarDrugs){
			List<Rating> ratingList = drugDiseaseRatingList.get(simDrug);
			if(ratingList != null){
				for(Rating rating:ratingList){
					Item recommendedItem = rating.getRecommendedItem();

					// insert into allRecommendedItems
					ArrayList<Rating> retRating = allRecommendedItems.get(recommendedItem);
					if(retRating==null){
						// no such entry 
						retRating = new ArrayList<Rating>();
						retRating.add(rating);
						allRecommendedItems.put(recommendedItem, retRating);
					} else {
						// update the entry
						retRating.add(rating);
						allRecommendedItems.put(recommendedItem, retRating);
					}
				}
			}
		}

		return allRecommendedItems;
	}



	/**
	 *  Get k-many similar Drugs 
	 * @param target
	 * @param numberOfSimilarDrugs
	 * @return
	 */
	protected ArrayList<Item> getMostSimilarDrugs(Item target, 
			Integer numberOfSimilarDrugs, ArrayList<SimType> fieldList) {
		
		// 2) Get other drugs similarities to the target
		List<Similarity> drugSimValsList = getDrugSimVals(target, fieldList);

		// 3) Get most similar users using multi-obj-opt
		ArrayList<Item> similarDrugs = getMostSimilarDrugs(parameters.prefferedMoType, 
				drugSimValsList, fieldList, numberOfSimilarDrugs);
	
		/*// create retList by reading the similar drugs info from db
		ArrayList<Drug> retList = createSimDrugsList(con, similarDrugs);*/

		return similarDrugs;
	}

	private ArrayList<Item> getMostSimilarDrugs(MOType prefferedMoType,
			List<Similarity> geneSimValsMap, ArrayList<SimType> fieldList,
			Integer numberOfSimilarDrugs) {
		ArrayList<Item> similarDrugs = null;

		switch(parameters.getPrefferedMoType()){
		case ONLYDOMINATES:
		{
			similarDrugs = getMostSimilarDrugs(geneSimValsMap, 
					fieldList, numberOfSimilarDrugs);
		}
		break;
		case KDOMINATES:
		{
			similarDrugs = new ArrayList<Item>();
			while(similarDrugs.size() < numberOfSimilarDrugs){
				ArrayList<Item> similarDrugsTemp	= getMostSimilarDrugs(similarDrugs, geneSimValsMap, 
						fieldList, numberOfSimilarDrugs);

				if(similarDrugsTemp.size() > 0 ) {
					int size = similarDrugsTemp.size();
					if((similarDrugs.size() + size) <= numberOfSimilarDrugs){
						similarDrugs.addAll(similarDrugsTemp);
					}else{
						for(int i = 0; i < size; i++){
							if(similarDrugs.size() < numberOfSimilarDrugs){
								Item similarDrug = similarDrugsTemp.get(i);
								similarDrugs.add(similarDrug);
							} else{
								break;
							}
						}
					}
				} else{
					break;
				}
			}	
		}
		break;
		case ATLEASTKDOMINATES:
		{
			similarDrugs = new ArrayList<Item>();
			while(similarDrugs.size() < numberOfSimilarDrugs){
				ArrayList<Item> similarDrugsTemp	= getMostSimilarDrugs(similarDrugs, geneSimValsMap, 
						fieldList, numberOfSimilarDrugs);

				if(similarDrugsTemp.size() > 0 ) {
					similarDrugs.addAll(similarDrugsTemp);
				} else{
					break;
				}
			}		
		}
		break;
		default: 
			System.out.println("Error in type of preffered MO type");
			break;
		}
		return similarDrugs;
	}

	public ArrayList<Item> getMostSimilarDrugs(ArrayList<Item> alreadySelected, 
			List<recRelated.Similarity> drugSimValsMap, ArrayList<Similarity.SimType> fieldsToUse, 
			Integer numberOfSimilarDrugs) {
		// remove elements(non-dominated users) which are already selected
		ArrayList<Similarity> drugSimValsMapPruned = new ArrayList<Similarity>();

		for(Similarity sim:drugSimValsMap){
			Item drug = sim.getItem();
			if(alreadySelected.contains(drug)){
				// do nothing
			} else{
				drugSimValsMapPruned.add(sim);
			}
		}

		// run normal getSimilar users & return
		return getMostSimilarDrugs(drugSimValsMapPruned, fieldsToUse, numberOfSimilarDrugs);
	}

	public ArrayList<Item> getMostSimilarDrugs(List<Similarity> drugSimValsMap, 
			ArrayList<Similarity.SimType> fieldsToUse, 
			Integer numberOfSimilarDrugs) {
		// 1) create dominance matrix
		MOBasedSimilarityCalculator moSimCalc = new MOBasedSimilarityCalculator();
		Double[][] dominanceMatrix = moSimCalc.createDominanceMatrix(drugSimValsMap, fieldsToUse);

		// 2) select non-dominated neighbours
		ArrayList<Similarity> nonDominatedSims = moSimCalc.findNonDominatedSims(drugSimValsMap, 
				dominanceMatrix);

		/*// 3) sort non-dominated neighbours -- TODO by what 
		ArrayList<Similarity.SimType> sortOrder = new ArrayList<Similarity.SimType>();
		ArrayList<Integer> similarUsers = moSimCalc.sortBy(nonDominatedSims, sortOrder, 
				numberOfSimilarUsers, userSimilarityThreshold);		*/

		ArrayList<Item> neighbours = new ArrayList<Item>();
		for(Similarity sim:nonDominatedSims){
			neighbours.add(sim.getItem());
		}

		return neighbours;


	}
	/**
	 * Get Drug simValues by reading  the related files
	 * @param target
	 * @param fieldList
	 * @return
	 */
	private List<Similarity> getDrugSimVals(Item target,
			ArrayList<SimType> fieldList) {
		ArrayList<Similarity> similarities = new ArrayList<>();

		try{
			// read similarities by field
			HashMap<SimType,HashMap<Item, Double>> simValByField = new HashMap<SimType, HashMap<Item,Double>>();

			for(SimType field:fieldList){
				HashMap<Item, Double> simValsOfOtherDrugs = getSimilarity(target, field);	
				simValByField.put(field, simValsOfOtherDrugs);
			}

			// create similarities by Drug name (for other calculations made later on)
			// create DrugName to field-simVal map
			simValByDrug = createSimValbyDrugMap(simValByField);

			for(Entry<Item, HashMap<SimType, Double>> e: simValByDrug.entrySet()){
				Similarity sim = new Similarity(e.getKey(),e.getValue());
				similarities.add(sim);
			}


		} catch(Exception e){
			e.printStackTrace();
		}

		return similarities;
	}

	/**
	 * 
	 * @param simValByField: simType--> <neighbor_drugItem, simVal>
	 * @return return map:  drugItem--> <simType, simVal>
	 */
	private HashMap<Item, HashMap<SimType, Double>> createSimValbyDrugMap(
			HashMap<SimType, HashMap<Item, Double>> simValByField) {
		HashMap<Item,HashMap<SimType, Double>> simValByDrug = new HashMap<Item, HashMap<SimType,Double>>();

		for(Entry<SimType, HashMap<Item, Double>> e : simValByField.entrySet()){
			SimType simType = e.getKey();
			HashMap<Item, Double> simVals = e.getValue();

			for(Entry<Item, Double> e2:simVals.entrySet()){
				Item drug = e2.getKey();
				Double simVal = e2.getValue();

				// insert to returning hashmap
				insertToSimilarityMap(simValByDrug, simType,drug,simVal);

			}
		}

		return simValByDrug;
	}

	/**
	 * 
	 * @param simValByDrug: drugItem--> <simType, simVal>
	 * @param simType: simType
	 * @param drug: drugItem
	 * @param simVal: similarity value
	 */
	private void insertToSimilarityMap(
			HashMap<Item, HashMap<SimType, Double>> simValByDrug,
			SimType simType, Item drug, Double simVal) {
		// control if any entry exists for this Drug
		HashMap<SimType, Double> drugNameEntry = simValByDrug.get(drug);
		if(drugNameEntry == null){
			// create new entry
			drugNameEntry = new HashMap<Similarity.SimType, Double>();
			drugNameEntry.put(simType, simVal);
			simValByDrug.put(drug, drugNameEntry);
		} else {
			// update entry
			drugNameEntry.put(simType, simVal);
			simValByDrug.put(drug, drugNameEntry);
		}
	}
	


	private HashMap<Item, Double> getSimilarity(Item target, SimType field) 
			throws NumberFormatException, IOException {
		// decide on fileName to use
		String fileName = Similarity.decideFileName(field, folderName);

		// set path
		String path = inputPath + fileName;

		// read file
		// Open the file
		FileInputStream fstream = new FileInputStream(path);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		// Read 1 line for header 
		String header = br.readLine();
		String[] splittedHeader = header.split("\t");
		// find index of target Drug
		Integer indexOfTargetDrug = findDrugIndex(target, splittedHeader);
		if(indexOfTargetDrug == null){
//			System.err.println("indexOfTargetDrug is null-"
//					+ " targetDrug: " + target
//					+ " fileName: " + fileName);
		}
		
		HashMap<Item, Double> simMap = new HashMap<Item, Double>();
		if(indexOfTargetDrug != null){
			//Read File Line By Line until the index of target
			String strLine = null;
			int readLineCount=0;
			while ((strLine = br.readLine()) != null)   // read info
			{
				readLineCount++;
				if(readLineCount == indexOfTargetDrug){
					// read related line, parse it and return output
					String[] splitted = strLine.split("\t");
					String stdNameTarget = splitted[0].replace("\"", "").toLowerCase().trim();

					for(int i = 1; i < splitted.length; i++){
						String drugName = splittedHeader[i].replace("\"", "").toLowerCase().trim();
						String valStr = splitted[i];
						Double value = 0.0;
						if(valStr.equals("NA") == false && 
								valStr.length() > 0){
							value = Double.valueOf(valStr);
						}
						
						// dont collect the sim of drug to itself 
						if(target.doesContain(drugName) == false){
							Item drug = Utils.getItem(MainDrugRec.allDrugs, drugName);
							if(drug == null){
								//System.out.println("fieldType: " + field.toString() + ", drug is not listed on allDrugs: " + drugName);
							} else {
								simMap.put(drug, value);
							}
						} 
					}

				}

			}
		}

		return simMap;
	}
	
	private Integer findDrugIndex(Item target, String[] splittedHeader) {
		Integer retVal = null;
		for(int i=0; i<splittedHeader.length; i++){
			String s = splittedHeader[i].replace("\"", "").toLowerCase().trim();
			if(target.doesContain(s)){
				retVal = i;
				break;
			}
		}

		return retVal;
	}

}
