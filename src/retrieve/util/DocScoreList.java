package retrieve.util;

import java.util.ArrayList;

public class DocScoreList {
	public int docid;
	public String docno;
	public ArrayList<Double> scores;
	
	public DocScoreList(int docid, ArrayList<Double> scores){
		this.docid = docid;
		this.scores = scores;
	}
	
	
}
