package retrieve.util;

import index.domain.Dictionary;
import index.domain.Document;
import index.domain.MyInteger;
import index.domain.PostingItem;
import index.domain.PostingList;
import index.domain.Token;
import index.util.TokenStream;
import index.util.Tokenizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
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
		String fname = "";
		test_loadDocTfIdfVectorLengths(fname);
	}
	
	/**
	 * Write 2 norm of tfidf vector of each document into file.
	 * @param dic invert index.
	 * @param N
	 * @param fname
	 * @throws IOException
	 */
	public static void writeDocTfIdfVectorLength(Dictionary dic, int N, String fname) throws IOException{
		double[] docWeightLengths = computeDocTfIdfVectorLengths(dic, N);
		BufferedWriter bufWriter = new BufferedWriter(new FileWriter(fname));
		// TODO 
		for(int i=0; i<N; i++){
			String entryStr = String.format("%.5f\n", docWeightLengths[i]);
			bufWriter.write(entryStr);
		}
		bufWriter.close();
	}
	
	
	public static void test_loadDocTfIdfVectorLengths(String fname) throws IOException{
		ArrayList<Double> dwl = loadDocTfIdfVectorLengths(fname);
		System.out.print(dwl.get(100));
	}
	/**
	 * Compute 2 norm of tfidf vector for each document. 
	 * @param dic
	 * @param N
	 * @return
	 */
	public static double[] computeDocTfIdfVectorLengths(Dictionary dic, int N){
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
		double[] docVectorLengths = new double[N]; 
		for (int i=0; i<N; i++){
			docVectorLengths[i] =  MyMath.euclideanLength(docVectors.get(i));
		}
		return docVectorLengths;
	}
	
	public static void computeWriteDocTfIdfVectorLengths(Dictionary dic, String[] docNoByDocId, int N, String docPath, String fname) throws IOException{
		// first decode all posting list and get df of term, record that
		HashMap<String, Integer> termDf = new HashMap<String, Integer>();
		Set<String> keySet = dic.getKeySet();
		for (String term: keySet){
			PostingList pl = dic.getPL(term);
			pl = PostingList.decode(pl.c_pl, new MyInteger(0));
			int df = pl.getDF();
			termDf.put(term, df);
		}
		System.out.println("termDf finished.");
		
		// then compute each document's vector and length
		//ArrayList<Double> docVectorLengths = new ArrayList<Double>();
		double[] docVectorLengths = new double[N*2]; int maxIndex = 0; 
		// need to scan through a document and meanwhile count the term frequency
		TokenStream ts = new TokenStream(docPath, "content");
		int docIdOnProcess = -1;
		HashMap<String, Integer> termsFreq = new HashMap<String, Integer>();
		while(ts.hasNext()){
			Token token = ts.next();
			token = token.preprocess();
			String term = token.term;
			if (token.docid != docIdOnProcess){
				if (docIdOnProcess != -1){
					// we are now at the first term of the next document
					// so first compute the length of previous document
					double l = docTfIdfVectorLength(termsFreq, termDf, docIdOnProcess, N);
					docVectorLengths[docIdOnProcess] = l;
					// and then add this first term into a new record.
					termsFreq = new HashMap<String, Integer>();
					termsFreq.put(term, 1);
					
					if (docIdOnProcess % 10000 == 0){
						System.out.println("# finished: " + docIdOnProcess);
					}
					//System.out.println("finished: DocId-" + docIdOnProcess + ", index - " + + nDocFinished);
					maxIndex = Math.max(docIdOnProcess, maxIndex);
				}
			}else{
				if(termsFreq.containsKey(term)){
					termsFreq.replace(term, termsFreq.get(term)+1);
				}else{
					termsFreq.put(term, 1);
				}
			}
			docIdOnProcess = token.docid;
			
		}
		double l = docTfIdfVectorLength(termsFreq, termDf, docIdOnProcess, N);
		docVectorLengths[docIdOnProcess] = l;
		maxIndex = Math.max(docIdOnProcess, maxIndex);
		
		
		
		BufferedWriter bufWriter = new BufferedWriter(new FileWriter(fname));
		System.out.println("N = " + N);
		System.out.println("#doc Vector = " + docVectorLengths.length);
		for(int i=0; i<maxIndex; i++){
			//String entryStr = String.format("%.3f\n", docVectorLengths[i]);
			String entryStr = String.format("%.3f\n", docVectorLengths[i]);
			bufWriter.write(entryStr);
		}
		bufWriter.close();
	}
	
	public static double docTfIdfVectorLength(HashMap<String, Integer> termsFreq, HashMap<String, Integer> termDf, int docId, int N) throws IOException{
		// compute length
		BufferedWriter bufWriter = new BufferedWriter(new FileWriter("i:\\kuaipan\\graduateCourses\\IR\\program\\IR_system\\log.txt", true));
		double norm = 0;
		Set<String> keys = termsFreq.keySet();
		for(String term: keys){
			if (!termDf.containsKey(term)){
				if (term.equals("\n") || term.equals(""))continue;
				bufWriter.append("docID: " + docId + " don't have term: '" + term + "'\n");
				continue;
			}
			int df = termDf.get(term);
			int tf = termsFreq.get(term);
			double tfidf = (1 + Math.log(tf)) * Math.log(N / df); 
			norm += tfidf * tfidf;
		}
		bufWriter.close();
		return Math.sqrt(norm);
	}
	
	/*
	 *TODO this method is very slow
	 */
	public static void computeWriteDocTfIdfVectorLengthsObsolete(Dictionary dic, String[] docNoByDocId, int N, String docPath, String fname) throws IOException{
		// first decode all posting list and get df of term, record that
		HashMap<String, Integer> termDf = new HashMap<String, Integer>();
		Set<String> keySet = dic.getKeySet();
		for (String term: keySet){
			PostingList pl = dic.getPL(term);
			pl = PostingList.decode(pl.c_pl, new MyInteger(0));
			int df = pl.getDF();
			termDf.put(term, df);
		}
		
		BufferedWriter bufWriter = new BufferedWriter(new FileWriter(fname));
		
		for(int i=0; i<N; i++){
			if(i%10000==0) System.out.println(i);
			Document doc = Document.getDocument(docNoByDocId[i], docPath); //获取第一篇文档
			double l = docTfIdfVectorLengthObsolete(doc.content, termDf, N);
			String entryStr = String.format("%.3f\n", l);
			bufWriter.write(entryStr);
		}
		bufWriter.close();
	}
	/*
	 * TODO this method is very slow
	 */
	public static double docTfIdfVectorLengthObsolete(String content, HashMap<String, Integer> termDf, int N) throws IOException{
		
		// get terms and term frequency
		MyInteger index = new MyInteger(0);
		Token token;
		HashMap<String, Integer> termsFreq = new HashMap<String, Integer>();
		while((token=Tokenizer.nextToken(content, 0, index)) != null){
			token = token.preprocess();
			if(token.term.equals("")) continue; //词条，停用词
			String term = token.term;
			if(termsFreq.containsKey(term)){
				termsFreq.replace(term, termsFreq.get(term)+1);
			}else{
				termsFreq.put(term, 1);
			}
		}
		
		// compute length
		double norm = 0;
		Set<String> keys = termsFreq.keySet();
		for(String term: keys){
			
			if (!termDf.containsKey(term)){
				System.out.println(term);
				continue;
			}
			int df = termDf.get(term);
			int tf = termsFreq.get(term);
			double tfidf = (1 + Math.log(tf)) * Math.log(N / df); 
			norm += tfidf * tfidf;
		}
		return Math.sqrt(norm);
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
