package retrieve;

import index.domain.Dictionary;
import index.domain.Document;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import ciir.umass.edu.eval.Evaluator;
import ciir.umass.edu.learning.RANKER_TYPE;
import retrieve.util.DocInfo;
import retrieve.util.DocScore;
import retrieve.util.DocScoreList;

public class Test {
	public static String projectPath = "i:\\kuaipan\\graduateCourses\\IR\\program\\IR_system\\";
	public static String dataPath = projectPath + "data\\";
	public static String indexPath = dataPath + "index\\";
	public static String docPath = dataPath + "WT10G\\WT10G_toy\\";
	public static String truthFileName= dataPath+"/WT10G/qrels.trec9_10";
	public static String outputFeaturesFileName = projectPath + "RankLib\\features_all.txt";
	public static String queryFileName = dataPath + "\\WT10G\\querys.txt";
	
	
	public static void main(String[] args) throws IOException{
		testLearnRank();
	}
	
	
	
	public static void testSearchAllQueries() throws IOException{
		Search s = new Search(indexPath);
		
		ArrayList<Integer> queryNums = new ArrayList<Integer>();
		ArrayList<String> querys = new ArrayList<String>();
		loadTestQuerys(queryFileName, queryNums, querys);
		String fname = "i:\\kuaipan\\graduateCourses\\IR\\program\\test_score2.txt";
		BufferedWriter bufWriter = new BufferedWriter(new FileWriter(fname)); // set FileWriter(fname, true) means append contents.
		
		for (int qi=0; qi<querys.size(); qi++){
			ArrayList<DocScoreList> scores = s.SearchFor(querys.get(qi));
			
			int n = Math.min(1000, scores.size());
			for (int di=0; di<n; di++){
				DocScoreList sc = scores.get(di);
				// queryid Q0  DOCNO   RANK    score   STANDARD
				String oneEntry = String.join("\t", queryNums.get(qi).toString(), "Q0", sc.docno, (di+1)+"", sc.scores.get(0).toString(), "Standard")+"\n"; 
				bufWriter.write(oneEntry);
			}
			
		}
		bufWriter.close();
	}
	
	public static void loadTestQuerys(String queryFileName, ArrayList<Integer> queryNums, ArrayList<String> querys) throws NumberFormatException, IOException{
		//String fname = "i:\\kuaipan\\graduateCourses\\IR\\program\\data\\WT10G\\querys.txt";
		BufferedReader bufReader = new BufferedReader(new FileReader(queryFileName));
		String line;
		while((line = bufReader.readLine()) != null){
			queryNums.add(Integer.parseInt(line.substring(0, 3)));
			querys.add(line.substring(5));
			//System.out.println(line);
		}
		bufReader.close();
		System.out.println("query files loaded");
	}
	
	public static void testSearch() throws IOException{
		Search S = new Search(indexPath);
		ArrayList<DocScoreList> docs = S.SearchFor("Algorithm Design");
		for (int i=0; i<10; i++){
			DocScoreList doc = docs.get(i);
			System.out.println(S.docNoByDocId[doc.docid] + ": " + doc.scores.toString());
			Document docObj = Document.getDocument(S.docNoByDocId[doc.docid], S.docPath);
			System.out.println(docObj.content);
			String title = docObj.title;
			System.out.println("-----------------\n---------------------");
		}
		System.out.println(S.docNoByDocId[docs.get(docs.size()-1).docid] + ": " + docs.get(docs.size()-1).scores.toString());
		// System.out.print(docs.toString());
	}
	
	public static void testLearnRank() throws IOException{
		Search s = new Search(indexPath);
		
		LearnRank R = new LearnRank();
		ArrayList<Integer> queryNums = new ArrayList<Integer>();
		ArrayList<String> querys = new ArrayList<String>();
		Test.loadTestQuerys(queryFileName, queryNums, querys);
		System.out.println("Querys loaded");
		
		for (int qi=80; qi<100; qi++){
			long t1 = new Date().getTime();
			ArrayList<DocScoreList> scores = s.SearchFor(querys.get(qi));
			
			long t2 = new Date().getTime();
			String[] docsNo = R.rank(queryNums.get(qi), scores); // docNo sort by rank score
			long t3 = new Date().getTime();
			long delta1 = t2-t1;
			long delta2 = t3 - t2;
			//System.out.println(String.join("-", t1+"", t1+"", t3+""));
			System.out.println(delta1 + "  " + delta2);
		}
	}
	
	
}
