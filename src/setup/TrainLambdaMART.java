package setup;

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

import retrieve.Search;
import retrieve.Test;
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
 * generate train file
 */
public class TrainLambdaMART {
	
	protected static RankerFactory rFact = new RankerFactory();
	protected MetricScorerFactory mFact = new MetricScorerFactory();
	//rType2[rankerType], trainMetric, testMetric
	public  Evaluator e;
	public  Ranker ranker;
	public  int[] features;
	
	public static void main(String[] args) throws NumberFormatException, IOException{
		//genTrainingData();
		//T.test_rank();
	}
	
	public void initialize(){
		
	}
	
	public void writeFeatures(int queryID, ArrayList<DocScoreList> scores, String featureFileName) throws IOException{
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
	
	public static void genTrainingData(String indexPath, String truthFileName, String queryFileName, String outputFeaturesFileName) throws NumberFormatException, IOException{
		Search S = new Search(indexPath);
		
		ArrayList<Integer> queryNums = new ArrayList<Integer>();
		ArrayList<String> querys = new ArrayList<String>();
		Test.loadTestQuerys(queryFileName, queryNums, querys);
		
		// truth : { queryId: {doc1No: rel, doc2No: rel, ...}, ...}
		HashMap<Integer, HashMap<String, Integer>> truth = new HashMap<Integer, HashMap<String, Integer>>();
		loadQueryGroundTruth(truthFileName, truth);
		
		BufferedWriter bufWriter = new BufferedWriter(new FileWriter(outputFeaturesFileName)); // set FileWriter(fname, true) means append contents.
		
		for (int qi=0; qi<50; qi++){
			String query = querys.get(qi);
			System.out.println(qi);
			ArrayList<DocScoreList> scores = S.SearchFor(query);
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
		System.out.println("query ground truth loaded");
	}
	

	
	
	
	
}
