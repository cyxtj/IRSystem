package retrieve;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import retrieve.util.DocScoreList;
import ciir.umass.edu.eval.Evaluator;
import ciir.umass.edu.learning.DataPoint;
import ciir.umass.edu.learning.DenseDataPoint;
import ciir.umass.edu.learning.RANKER_TYPE;
import ciir.umass.edu.learning.RankList;
import ciir.umass.edu.learning.Ranker;
import ciir.umass.edu.learning.RankerFactory;
import ciir.umass.edu.metric.MetricScorerFactory;
import ciir.umass.edu.utilities.MergeSorter;
import ciir.umass.edu.utilities.RankLibError;
import ciir.umass.edu.utilities.SimpleMath;

public class LearnRank {
	public Ranker ranker;
	protected static RankerFactory rFact = new RankerFactory();
	//protected MetricScorerFactory mFact = new MetricScorerFactory();
	
	public void LearnRank(){
		initialize();
	}
	
	public void initialize(){
		String modelFile = "i:\\kuaipan\\graduateCourses\\IR\\program\\RankLib\\mymodel-50.txt";
		String[] rType = new String[]{"MART", "RankNet", "RankBoost", "AdaRank", "Coordinate Ascent", "LambdaRank", "LambdaMART", "ListNet", "Random Forests", "Linear Regression"};
		RANKER_TYPE[] rType2 = new RANKER_TYPE[]{RANKER_TYPE.MART, RANKER_TYPE.RANKNET, RANKER_TYPE.RANKBOOST, RANKER_TYPE.ADARANK, RANKER_TYPE.COOR_ASCENT, RANKER_TYPE.LAMBDARANK, RANKER_TYPE.LAMBDAMART, RANKER_TYPE.LISTNET, RANKER_TYPE.RANDOM_FOREST, RANKER_TYPE.LINEAR_REGRESSION};
		int rankerType = 6;
		String trainMetric = "NDCG@20";
		String testMetric = "MAP";
		//e = new Evaluator(rType2[rankerType], trainMetric, testMetric);
		ranker = rFact.loadRankerFromFile(modelFile);
		//features = ranker.getFeatures();
		System.out.println("Model loaded");
		
	}
	
	public void rank(int queryID, ArrayList<DocScoreList> scores) throws IOException{
		String indriRanking = "i:\\kuaipan\\graduateCourses\\IR\\program\\RankLib\\ranks_one_query.txt" + queryID;
		
		double[] newScores = new double[scores.size()];
		RankList l = toRankList(queryID, scores);
		int[] rankIndex = rank(l, newScores);
		writeRankResult(l, indriRanking, newScores, rankIndex);
		
	}
	
	public int[] rank(RankList l, double[] scores){
		//double[] scores = new double[l.size()];
		for (int j = 0; j < l.size(); j++)
			scores[j] = ranker.eval(l.get(j));
		int[] rankIndex = MergeSorter.sort(scores, false);
		return rankIndex;
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
	
	public RankList toRankList(int queryID, ArrayList<DocScoreList> scores){
		List<DataPoint> rl = new ArrayList<DataPoint>();
		for (DocScoreList score: scores){
			DataPoint qp = toDataPoint(queryID, score);
			rl.add(qp);
		}
		return new RankList(rl);
	}
	
	
	public DataPoint toDataPoint(int queryID, DocScoreList score){
		int featureNumber = score.scores.size();
		float[] fVals = new float[featureNumber+1];
		int i = 1;
		for (Double s: score.scores){
			fVals[i] = s.floatValue();
			i ++;
		}
		return new DenseDataPoint(fVals, featureNumber, queryID, 0, score.docno);
		
	}
}
