package method;

public class DrugRecommenderParameters {
	public enum MOType{ONLYDOMINATES, KDOMINATES, ATLEASTKDOMINATES;}
	public enum OutlistType{FIXEDLENGTH, THRESHOLDBASED;}
	public enum ISType{SUM, AVG, MAX, WEIGHTEDAVG, WEIGHTEDSUM;}
	
	// defult parameters
	public static Integer numberOfSimilarsDefault = -1;
	public static Integer outputListSizeDefault = -1; 
	public static Double thresholdDefault = -1.0;
	public static MOType moTypeDefault = MOType.ONLYDOMINATES;
	public static ISType isTypeDefault = ISType.SUM;
	public static OutlistType outlistTypeDefault = OutlistType.THRESHOLDBASED;

	// params	
	protected Integer numberOfSimilarItems;
	protected Integer outputListSize;
	protected Double threshold;
	protected MOType prefferedMoType;
	protected OutlistType outlistType;
	protected ISType itemSelectionType;

	// 
	public DrugRecommenderParameters(Integer numberOfSimilarItems, 
			Integer outputListSize, Double threshold, 
			MOType prefferedMoType, OutlistType outlistType, 
			ISType itemSelectionType) {
		super();
		this.numberOfSimilarItems = numberOfSimilarItems;
		this.outputListSize = outputListSize;
		this.threshold = threshold;
		this.prefferedMoType = prefferedMoType;
		this.outlistType = outlistType;
		this.itemSelectionType = itemSelectionType;
	}

	public Integer getNumberOfSimilarItems() {
		return numberOfSimilarItems;
	}

	public void setNumberOfSimilarItems(Integer numberOfSimilars) {
		this.numberOfSimilarItems = numberOfSimilars;
	}

	public Integer getOutputListSize() {
		return outputListSize;
	}

	public void setOutputListSize(Integer outputListSize) {
		this.outputListSize = outputListSize;
	}

	public Double getThreshold() {
		return threshold;
	}

	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}

	public MOType getPrefferedMoType() {
		return prefferedMoType;
	}

	public void setPrefferedMoType(MOType prefferedMoType) {
		this.prefferedMoType = prefferedMoType;
	}

	public OutlistType getOutlistType() {
		return outlistType;
	}

	public void setOutlistType(OutlistType outlistType) {
		this.outlistType = outlistType;
	}

	public ISType getItemSelectionType() {
		return itemSelectionType;
	}

	public void setItemSelectionType(ISType itemSelectionType) {
		this.itemSelectionType = itemSelectionType;
	}

	@Override
	public String toString() {
		return "DrugRecommenderParameters [numberOfSimilarItems=" + numberOfSimilarItems + ", outputListSize="
				+ outputListSize + ", threshold=" + threshold + ", prefferedMoType=" + prefferedMoType
				+ ", outlistType=" + outlistType + ", itemSelectionType=" + itemSelectionType + "]";
	}
	
}
