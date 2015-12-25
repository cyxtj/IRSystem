package retrieve;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import retrieve.util.DocScoreList;
import ciir.umass.edu.learning.DataPoint;
import ciir.umass.edu.learning.DenseDataPoint;
import ciir.umass.edu.learning.RankList;
import ciir.umass.edu.learning.Ranker;
import ciir.umass.edu.learning.RankerFactory;
import ciir.umass.edu.utilities.MergeSorter;
import ciir.umass.edu.utilities.RankLibError;
import ciir.umass.edu.utilities.SimpleMath;

public class LearnRank {
	public Ranker ranker;
	protected static RankerFactory rFact = new RankerFactory();
	
	public LearnRank(String modelFile){
		initialize(modelFile);
	}
	
	public void initialize(String modelFile){
		
		ranker = rFact.loadRankerFromFile(modelFile);
		System.out.println("Model loaded");
	}
	
	public String[] rank(int queryID, ArrayList<DocScoreList> docFeatures, String resultFileName) throws IOException{
		
		double[] newScores = new double[docFeatures.size()];
		RankList l = toRankList(queryID, docFeatures);
		int[] rankIndex = rank(l, newScores);
		String[] docsNo = new String[rankIndex.length];
		for (int i=0; i<rankIndex.length; i++){
			docsNo[i] = l.get(rankIndex[i]).getDescription();
		}
		if (resultFileName!=null){
			writeRankResult(l, resultFileName, newScores, rankIndex);
		}
		return docsNo;
	}
	
	public int[] rank(RankList l, double[] scores){
		//double[] scores = new double[l.size()];
		for (int j = 0; j < l.size(); j++)
			scores[j] = ranker.eval(l.get(j));
		int[] rankIndex = MergeSorter.sort(scores, false);
		return rankIndex;
	}
	
	
	public RankList toRankList(int queryID, ArrayList<DocScoreList> docFeatures){
		List<DataPoint> rl = new ArrayList<DataPoint>();
		for (DocScoreList docFeature: docFeatures){
			DataPoint qp = toDataPoint(queryID, docFeature);
			rl.add(qp);
		}
		return new RankList(rl);
	}
	
	
	public DataPoint toDataPoint(int queryID, DocScoreList docFeature){
		int featureNumber = docFeature.scores.size();
		float[] fVals = new float[featureNumber+1];
		int i = 1;
		for (Double s: docFeature.scores){
			fVals[i] = s.floatValue();
			i ++;
		}
		return new DenseDataPoint(fVals, featureNumber, queryID, 0, docFeature.docno);
		
	}
	
	
	public void writeRankResult(RankList l, String outputFileName, double[] scores, int[] idx){
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName), "UTF-8"));
			
			for (int j = 0; j < idx.length; j++) {
				int k = idx[j];
				String str = l.getID() + " Q0 " + l.get(k).getDescription().replace("#", "").trim() + " " + (j + 1) + " " + SimpleMath.round(scores[k], 5) + " indri";
				out.write(str);
				out.newLine();
			}
			out.close();
		}
		catch(IOException ex)
		{
			throw RankLibError.create("Error in writeRankResult: ", ex);
		}
	}
}
