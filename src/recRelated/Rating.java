package recRelated;

import drugRelated.Item;

public class Rating {
	Item recommenderItem;
	Item recommendedItem; 
	Double ratingScore;
	
	public Rating(Item recommender, Item recommended,
			Double ratingScore) {
		super();
		this.recommenderItem = recommender;
		this.recommendedItem = recommended;
		this.ratingScore = ratingScore;
	}

	public Item getRecommenderItem() {
		return recommenderItem;
	}

	public void setRecommenderItem(Item recommenderItem) {
		this.recommenderItem = recommenderItem;
	}

	public Item getRecommendedItem() {
		return recommendedItem;
	}

	public void setRecommendedItem(Item recommendedItem) {
		this.recommendedItem = recommendedItem;
	}

	public Double getRatingScore() {
		return ratingScore;
	}

	public void setRatingScore(Double ratingScore) {
		this.ratingScore = ratingScore;
	}

	@Override
	public String toString() {
		return "Rating [recommenderItem=" + recommenderItem + ", recommendedItem=" + recommendedItem + ", ratingScore="
				+ ratingScore + "]";
	}
	
	
}
