package retrieve;

import index.domain.Dictionary;
import index.domain.PostingItem;
import index.domain.PostingList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import common.MyMath;
import retrieve.util.DocInfo;
import retrieve.util.DocScoreList;
import retrieve.util.TermFreq;

public class Measure {
	public static double N;
	// BM25 parameters
	public static final double b = 0.75;
	public static final double k1 = 2;
	public static final double k3 = 1.5;
	public static final double avg_l = 400.63543;
	
	
	public static Dictionary dic;
	public static int[] docLengths;
	public static String[] docNoByDocId;
	public static ArrayList<Double> docVectorLengths;
	public static ArrayList<Double> docPageRanks;
	public static ArrayList<Double> docUrlDepths;
	
	
	public static void initialize(Dictionary dic, int[] docLengths, String[] docNoByDocId, double N) throws IOException{
		Measure.dic = dic;
		Measure.docLengths = docLengths;
		Measure.docNoByDocId = docNoByDocId;
		Measure.N = N;
		String indexPath = "i:\\kuaipan\\graduateCourses\\IR\\program\\data\\index";
		String docPath = "i:\\kuaipan\\graduateCourses\\IR\\program\\data\\WT10G\\WT10G_copy";
		//DocInfo.writeDocWeightLengths(dic, N, "i:\\kuaipan\\graduateCourses\\IR\\program\\Search\\WT10G\\docWeightLength.txt");
		docVectorLengths = DocInfo.loadDocTfIdfVectorLengths(indexPath+"/docWeightLength.txt");
		docPageRanks = new ArrayList<Double>();
		docUrlDepths = new ArrayList<Double>();
		DocInfo.loadDocPageRank("i:\\kuaipan\\graduateCourses\\IR\\program\\staticScores\\pagerank_in_byid.txt", docPageRanks);
		DocInfo.loadDocPageRank("i:\\kuaipan\\graduateCourses\\IR\\program\\staticScores\\urldepth_byid.txt", docUrlDepths);
	}
	
	
	public static ArrayList<DocScoreList> ComputeScores(ArrayList<TermFreq> queryTermsFreq, ArrayList<PostingList> PLs){
		HashMap<Integer, Double> bm25 = Measure.ComputeBM25(queryTermsFreq, PLs);
		HashMap<Integer, Double> vsm = Measure.ComputeVSM(queryTermsFreq, PLs);
		HashMap<Integer, Double[]> staticScore = Measure.ComputeStaticScores(PLs);
		ArrayList<DocScoreList> relatedDocidScores = Measure.combineScores(bm25, vsm, staticScore);
		return relatedDocidScores;
	}
	
	
	public static HashMap<Integer, Double> ComputeVSM(ArrayList<TermFreq> queryTermsFreq, ArrayList<PostingList> pls){
		// TODO check the formula
		
		HashMap<Integer, double[]> docVectors = new HashMap<Integer, double[]>(); // docid: [term1weight, term2weight, ...]
		double[] queryVector = new double[pls.size()];
		// Compute query vector and doc vectors
		// length of each vector is exactly the length of query terms.
		for (int i=0; i<pls.size(); i++){
			PostingList pl = pls.get(i);
			int df = pl.getDF();
			Integer queryTf = queryTermsFreq.get(i).freq;
			double queryTfIdf = (1+Math.log(queryTf)) * Math.log(N/df) * 1; // ltn (log tf, invert df, length=1)
			queryVector[i] =  queryTfIdf;
			for (int di=0, n=pl.getSize(); di<n; di++){
				PostingItem postingitem = pl.getItem(di);
				int tf = postingitem.tf;
				int docid = postingitem.docid;
				// int ld = docLengths[docid];
				double docTfIdf = (1+Math.log(tf)) * 1 / docVectorLengths.get(docid);  // lnc (log tf, df=1, cosine length).
				if (docVectors.containsKey(docid)){
					docVectors.get(docid)[i] = docTfIdf;
				} else{
					double[] vector = new double[pls.size()];
					vector[i] = docTfIdf;
					docVectors.put(docid, vector);
				}
			}
		}
		// compute the cosine distance.
		HashMap<Integer, Double> Scores = new HashMap<Integer, Double> ();
		for (Entry<Integer, double[]> entry : docVectors.entrySet()) {
			Integer docid = entry.getKey();
			double[] docVector = entry.getValue();
			double distance = MyMath.innerProduct(docVector, queryVector);
			Scores.put(docid, distance);
		}
		return Scores;
	}
	

	public static HashMap<Integer, Double> ComputeBM25(ArrayList<TermFreq> queryTerms, ArrayList<PostingList> PostingLists){
		double qtf = 1;
		
		// compute scores for each document
		// outer loop on query terms and inner loop on docids of the corresponding postinglist
		// at each inner loop we compute the score w(term, doc) and add to the document by hash
		HashMap<Integer, Double> docScores = new HashMap<Integer, Double>();
		for (int i=0; i<PostingLists.size(); i++){
			PostingList pl = PostingLists.get(i);
			int df = pl.getDF();
			for (int di=0, n=pl.getSize(); di<n; di++){
				PostingItem postingitem = pl.getItem(di);
				int tf = postingitem.tf;
				int docid = postingitem.docid;
				int ld = docLengths[docid]; 
				double w1 = Math.log((N-df+0.5)/(df+0.5));
				double w_t_d = qtf/(k3+qtf) * k1*tf/(tf+k1*(1-b+b*ld/avg_l)) * w1;
				if (docScores.containsKey(docid)){
					double value = docScores.get(docid)+w_t_d;
					docScores.put(docid, value);
				} else{
					docScores.put(docid, w_t_d);
				}
			}
		}
		return docScores;
	}
	
	/**
	 * Static scores include pagerank score and url depth.
	 * @param PostingLists
	 * @return
	 */
	public static HashMap<Integer, Double[]> ComputeStaticScores(ArrayList<PostingList> PostingLists){
		
		HashMap<Integer, Double[]> docScores = new HashMap<Integer, Double[]>();
		for (int i=0; i<PostingLists.size(); i++){
			PostingList pl = PostingLists.get(i);
			for (int di=0, n=pl.getSize(); di<n; di++){
				PostingItem postingitem = pl.getItem(di);
				int docid = postingitem.docid;
				
				if (docScores.containsKey(docid)){
					continue;
				} else{
					double pr = docPageRanks.get(docid);
					double urldepth = docUrlDepths.get(docid);
					Double[] scores = {pr, urldepth, pr, urldepth};
					docScores.put(docid, scores);
				}
			}
		}
		return docScores;
	}
	
	

	public static ArrayList<DocScoreList> combineScores(HashMap<Integer, Double> bm25, HashMap<Integer, Double> vsm, HashMap<Integer, Double[]> staticScore){
		if (!(bm25.size()==vsm.size() && bm25.size()==staticScore.size())){
			return null;
		}
		ArrayList<DocScoreList> docsScores = new ArrayList<DocScoreList>();
		
		Iterator<Entry<Integer, Double>> iter = bm25.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) iter.next();
			Integer docid = entry.getKey();
			Double bm25val = entry.getValue();
			Double vsmval = vsm.get(docid);
			Double[] staticvals = staticScore.get(docid);
			
			ArrayList<Double> scores = new ArrayList<Double>();
			scores.add(bm25val);
			scores.add(vsmval);
			for (Double s: staticvals){
				scores.add(s);
			}
			DocScoreList docScores = new DocScoreList(docid, scores);
			docScores.docno = docNoByDocId[docid];
			docsScores.add(docScores);
		}
		return docsScores;
	}
}
