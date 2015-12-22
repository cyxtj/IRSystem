package retrieve;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import retrieve.util.DocScore;
import retrieve.util.DocScoreList;

public class Test {
	public static void main(String[] args) throws IOException{
		Search.initialize();
		ArrayList<Integer> queryNums = new ArrayList<Integer>();
		ArrayList<String> querys = new ArrayList<String>();
		loadTestQuerys(queryNums, querys);
		String fname = "i:\\kuaipan\\graduateCourses\\IR\\program\\test_score2.txt";
		BufferedWriter bufWriter = new BufferedWriter(new FileWriter(fname)); // set FileWriter(fname, true) means append contents.
		
		for (int qi=0; qi<querys.size(); qi++){
			ArrayList<DocScoreList> scores = Search.SearchFor(querys.get(qi));
			scores = Rank.rank(scores);
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
	
	public static void loadTestQuerys(ArrayList<Integer> queryNums, ArrayList<String> querys) throws NumberFormatException, IOException{
		String fname = "i:\\kuaipan\\graduateCourses\\IR\\program\\data\\WT10G\\querys.txt";
		BufferedReader bufReader = new BufferedReader(new FileReader(fname));
		String line;
		while((line = bufReader.readLine()) != null){
			queryNums.add(Integer.parseInt(line.substring(0, 3)));
			querys.add(line.substring(5));
			//System.out.println(line);
		}
		bufReader.close();
	}
}
