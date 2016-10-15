package recRelated;

import java.util.Comparator;

import drugRelated.Item;

public class Recommendation {

	Item recommendedItem;
	Double score;
	
	
	
	public Recommendation(Item recommendedItem, Double score) {
		super();
		this.recommendedItem = recommendedItem;
		this.score = score;
	}

	public Item getRecommendedItem() {
		return recommendedItem;
	}



	public void setRecommendedItem(Item recommendedItem) {
		this.recommendedItem = recommendedItem;
	}



	public Double getScore() {
		return score;
	}



	public void setScore(Double score) {
		this.score = score;
	}



	@Override
	public String toString() {
		return "Recommendation [recommendedItem=" + recommendedItem + ", score=" + score + "]";
	}



	public static Comparator<Recommendation> ScoreComparator = new Comparator<Recommendation>() {

		public int compare(Recommendation o1, Recommendation o2) {
			return o2.getScore().compareTo(o1.getScore());
		}

	};
}
