package retrieve.util;

import java.util.Comparator;

public class DocScoreListComparator implements Comparator<DocScoreList>{
	@Override
	public int compare(DocScoreList d1, DocScoreList d2){
		double x1 = d1.scores.get(0);
		double x2 = d2.scores.get(0);
		if (x1 == x2){return 0;}
		return x1 < x2 ? 1: -1;
	}

}
