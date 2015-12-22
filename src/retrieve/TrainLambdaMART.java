package retrieve;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrieve.util.DocInfo;
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

/**
 * @author Cao Yixuan
 * 
 * This class provide a interface to RankLib package to train the model without cmd.
 * Not Finished!!!
 * Maybe use cmd-line at present.
 * e.score(savedModelFile, rankFile, scoreFile); in Evaluator.java can be used to rank a file give a model
 */
public class TrainLambdaMART {
	
	public static void main(String[] args) throws NumberFormatException, IOException{
		// genTrainingData();
		test_rank();
	}
	protected static RankerFactory rFact = new RankerFactory();
	protected MetricScorerFactory mFact = new MetricScorerFactory();
	//rType2[rankerType], trainMetric, testMetric
	public static Evaluator e;
	public static Ranker ranker;
	public static int[] features;
	
	
	public static void test_rank() throws IOException{
		Search.initialize();
		
		String modelFile = "i:\\kuaipan\\graduateCourses\\IR\\program\\RankLib\\mymodel-50.txt";
		String[] rType = new String[]{"MART", "RankNet", "RankBoost", "AdaRank", "Coordinate Ascent", "LambdaRank", "LambdaMART", "ListNet", "Random Forests", "Linear Regression"};
		RANKER_TYPE[] rType2 = new RANKER_TYPE[]{RANKER_TYPE.MART, RANKER_TYPE.RANKNET, RANKER_TYPE.RANKBOOST, RANKER_TYPE.ADARANK, RANKER_TYPE.COOR_ASCENT, RANKER_TYPE.LAMBDARANK, RANKER_TYPE.LAMBDAMART, RANKER_TYPE.LISTNET, RANKER_TYPE.RANDOM_FOREST, RANKER_TYPE.LINEAR_REGRESSION};
		int rankerType = 6;
		String trainMetric = "NDCG@20";
		String testMetric = "MAP";
		e = new Evaluator(rType2[rankerType], trainMetric, testMetric);
		ranker = rFact.loadRankerFromFile(modelFile);
		features = ranker.getFeatures();
		System.out.println("Model loaded");
		
		ArrayList<Integer> queryNums = new ArrayList<Integer>();
		ArrayList<String> querys = new ArrayList<String>();
		Test.loadTestQuerys(queryNums, querys);
		System.out.println("Querys loaded");
		
		for (int qi=80; qi<100; qi++){
			long t1 = new Date().getTime();
			ArrayList<DocScoreList> scores = Search.SearchFor(querys.get(qi));
			scores = Rank.rank(scores);
			long t2 = new Date().getTime();
			rank(queryNums.get(qi), scores);
			long t3 = new Date().getTime();
			long delta1 = t2-t1;
			long delta2 = t3 - t2;
			System.out.println(String.join("-", t1+"", t1+"", t3+""));
			System.out.println(delta1 + "  " + delta2);
		}
	}
	
	
	public static void writeFeatures(int queryID, ArrayList<DocScoreList> scores, String featureFileName) throws IOException{
		BufferedWriter bufWriter = new BufferedWriter(new FileWriter(featureFileName)); // set FileWriter(fname, true) means append contents.
		
		int n = scores.size();
		for (int di=0; di<n; di++){
			DocScoreList sc = scores.get(di);
			int rel = 0;
			// File Format
			// truth qid:xx feature1:xx feature2: 
			// 1 qid:3 1:0 2:1 3:1 4:0.5 5:0 # 3D
			String oneEntry = rel + " qid:" + queryID + " ";
			for (int si=0; si<sc.scores.size(); si++){
				oneEntry = oneEntry.concat(String.format("%d:%.4f ", si+1, sc.scores.get(si)));
			}
			bufWriter.write(oneEntry + " #" + sc.docno + "\n");
		}
		bufWriter.close();
	}
	
	public static void genTrainingData() throws NumberFormatException, IOException{
		Search.initialize();
		ArrayList<Integer> queryNums = new ArrayList<Integer>();
		ArrayList<String> querys = new ArrayList<String>();
		Test.loadTestQuerys(queryNums, querys);
		
		// truth : { queryId: {doc1No: rel, doc2No: rel, ...}, ...}
		String truth_fname = "i:\\kuaipan\\graduateCourses\\IR\\program\\data\\WT10G\\qrels.trec9_10";
		HashMap<Integer, HashMap<String, Integer>> truth = new HashMap<Integer, HashMap<String, Integer>>();
		loadQueryGroundTruth(truth_fname, truth);
		
		String fname = "i:\\kuaipan\\graduateCourses\\IR\\program\\RankLib\\features.txt";
		BufferedWriter bufWriter = new BufferedWriter(new FileWriter(fname)); // set FileWriter(fname, true) means append contents.
		
		for (int qi=0; qi<80; qi++){
			ArrayList<DocScoreList> scores = Search.SearchFor(querys.get(qi));
			scores = Rank.rank(scores);
			HashMap<String, Integer> queryTruth = truth.get(queryNums.get(qi));
			
			int n = scores.size();
			for (int di=0; di<n; di++){
				DocScoreList sc = scores.get(di);
				int rel;
				// only write the first 1000 not relevant and all relevant files.
				if (queryTruth.containsKey(sc.docno)){
					rel = queryTruth.get(sc.docno);
				} else {
					if (di > 1000){ 
						continue;
					}
					rel = 0;
				}
				// File Format
				// truth qid:xx feature1:xx feature2: 
				// 1 qid:3 1:0 2:1 3:1 4:0.5 5:0 # 3D
				String oneEntry = rel + " qid:" + queryNums.get(qi) + " ";
				for (int si=0; si<sc.scores.size(); si++){
					oneEntry = oneEntry.concat(String.format("%d:%.4f ", si+1, sc.scores.get(si)));
				}
				bufWriter.write(oneEntry + "\n");
			}
			
		}
		bufWriter.close();
	}
	
	/**
	 * load standard qrel file.
	 * @param fname
	 * @param truth
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void loadQueryGroundTruth(String fname, HashMap<Integer, HashMap<String, Integer>> truth) throws NumberFormatException, IOException{
		BufferedReader bufReader = new BufferedReader(new FileReader(fname));
		String line;
		while((line = bufReader.readLine()) != null){
			String[] splited = line.split(" ");
			Integer queryId = Integer.parseInt(splited[0]);
			String docNo = splited[2];
			Integer rel = Integer.parseInt(splited[3]);
			if (truth.containsKey(queryId)){
				truth.get(queryId).put(docNo, rel);
			}else{
				HashMap<String, Integer> map = new HashMap<String, Integer>();
				map.put(docNo, rel);
				truth.put(queryId, map);
			}
		}
		bufReader.close();
	}
	

	public static void rank(int queryID, ArrayList<DocScoreList> scores) throws IOException{
		String indriRanking = "i:\\kuaipan\\graduateCourses\\IR\\program\\RankLib\\ranks_one_query.txt" + queryID;
		
		double[] newScores = new double[scores.size()];
		RankList l = toRankList(queryID, scores);
		int[] rankIndex = rank(l, newScores);
		writeRankResult(l, indriRanking, newScores, rankIndex);
		
	}
	
	public static int[] rank(RankList l, double[] scores){
		//double[] scores = new double[l.size()];
		for (int j = 0; j < l.size(); j++)
			scores[j] = ranker.eval(l.get(j));
		int[] rankIndex = MergeSorter.sort(scores, false);
		return rankIndex;
	}
	
	public static void writeRankResult(RankList l, String outputFileName, double[] scores, int[] idx){
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
	
	public static RankList toRankList(int queryID, ArrayList<DocScoreList> scores){
		List<DataPoint> rl = new ArrayList<DataPoint>();
		for (DocScoreList score: scores){
			DataPoint qp = toDataPoint(queryID, score);
			rl.add(qp);
		}
		return new RankList(rl);
	}
	
	
	public static DataPoint toDataPoint(int queryID, DocScoreList score){
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
