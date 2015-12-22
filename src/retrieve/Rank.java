package retrieve;

import java.util.ArrayList;

import retrieve.util.DocScoreList;
import retrieve.util.DocScoreListComparator;

public class Rank {
	
	public static ArrayList<DocScoreList> rank(ArrayList<DocScoreList> docsScores){
		DocScoreListComparator dslc = new DocScoreListComparator();
		docsScores.sort(dslc);
		return docsScores;
	}
}
