package recRelated;

import java.util.Comparator;


public class RecommendationComparator implements Comparator<Recommendation> {

	@Override
	public int compare(Recommendation r1, Recommendation r2) {
		if(r1.getScore() > r2.getScore()){
			return -1;
		} else if(r1.getScore() < r2.getScore()){
			return 1;
		}
        return 0;
	}

}
