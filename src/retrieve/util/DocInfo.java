package retrieve.util;

import index.domain.Dictionary;
import index.domain.MyInteger;
import index.domain.PostingItem;
import index.domain.PostingList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import common.MyMath;

public class DocInfo {
	public int docid;
	public int length;
	public double vectorLength;
	public double pagerank;
	public int urlDepth;
	
	public DocInfo(){}
	public DocInfo(int docid, int length, double vectorLength, double pagerank, int urlDepth){
		this.docid = docid;
		this.length = length;
		this.vectorLength = vectorLength;
		this.pagerank = pagerank;
		this.urlDepth = urlDepth;
	}
	
	
	public static void main(String[] args) throws IOException{
		test_loadDocTfIdfVectorLengths();
	}
	
	/**
	 * Write 2 norm of tfidf vector of each document into file.
	 * @param dic invert index.
	 * @param N
	 * @param fname
	 * @throws IOException
	 */
	public static void writeDocTfIdfVectorLength(Dictionary dic, double N, String fname) throws IOException{
		ArrayList<Double> docWeightLengths = computeDocTfIdfVectorLengths(dic, N);
		BufferedWriter bufWriter = new BufferedWriter(new FileWriter(fname));
		// TODO 
		for(int i=0; i<N; i++){
			String entryStr = String.format("%.5f\n", docWeightLengths.get(i));
			bufWriter.write(entryStr);
		}
		bufWriter.close();
	}
	
	
	public static void test_loadDocTfIdfVectorLengths() throws IOException{
		ArrayList<Double> dwl = loadDocTfIdfVectorLengths("i:\\kuaipan\\graduateCourses\\IR\\program\\Search\\WT10G\\docWeightLength.txt");
		System.out.print(dwl.get(100));
	}
	/**
	 * Compute 2 norm of tfidf vector for each document. 
	 * @param dic
	 * @param N
	 * @return
	 */
	public static ArrayList<Double> computeDocTfIdfVectorLengths(Dictionary dic, double N){
		HashMap<Integer, ArrayList<Double>> docVectors = new HashMap<Integer, ArrayList<Double>>();
		Set<String> termsSet = dic.getKeySet();
		for (String term: termsSet){
			PostingList pl = dic.getPL(term);
			pl = PostingList.decode(pl.c_pl, new MyInteger(0));
			for (PostingItem pi: pl.list){
				int docid = pi.docid;
				double df = pl.getDF();
				double tfidf = pi.tf * Math.log(N / df); 
				if (docVectors.containsKey(docid)){
					docVectors.get(docid).add(tfidf);
				}else{
					ArrayList<Double> termList = new ArrayList<Double>();
					termList.add(tfidf);
					docVectors.put(docid, termList);
				}
			}
		}
		/*
		HashMap<Integer, Double> docWeightLengths = new	HashMap<Integer, Double>();
		Set<Entry<Integer, ArrayList<Double>>> iter = docVectors.entrySet();
		for (Entry<Integer, ArrayList<Double>> d: iter){
			Integer docid = d.getKey();
			ArrayList<Double> vector = d.getValue();
			docWeightLengths.put(docid, MyMath.euclideanLength(vector));
		}
		System.out.println("#doc has tfidf vector"+docWeightLengths.size());
		*/
		ArrayList<Double> docVectorLengths = new ArrayList<Double>((int) N); 
		for (int i=0; i<N; i++){
			docVectorLengths.set(i, MyMath.euclideanLength(docVectors.get(i)));
		}
		return docVectorLengths;
	}
	
	/**
	 * Load 2 norm of tfidf vector of each document.
	 * @param fname file name to read
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<Double> loadDocTfIdfVectorLengths(String fname) throws IOException{
		ArrayList<Double> docVectorLengths  = new ArrayList<Double>();
		BufferedReader bufReader = new BufferedReader(new FileReader(fname));
		String line;
		while((line = bufReader.readLine()) != null){
			// TODO  What's wrong with 'null'???
			
			if (line.endsWith("null")){
				line = "1";
			}
			//System.out.println(line);
			docVectorLengths.add(Double.parseDouble(line));
		}
		bufReader.close();
		return docVectorLengths;
	}
	
	public static void loadDocPageRank(String fname, ArrayList<Double> docPageRanks) throws IOException{
		BufferedReader bufReader = new BufferedReader(new FileReader(fname));
		String line;
		while((line = bufReader.readLine()) != null){
			// System.out.println(line);
			docPageRanks.add(Double.parseDouble(line));
		}
		bufReader.close();
	}
	
	public static void loadDocUrlDepth(String fname, ArrayList<Double> docUrlDepths) throws IOException{
		BufferedReader bufReader = new BufferedReader(new FileReader(fname));
		String line;
		while((line = bufReader.readLine()) != null){
			//System.out.println(line);
			docUrlDepths.add(Double.parseDouble(line));
		}
		bufReader.close();
	}
	
}
