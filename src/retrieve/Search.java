package retrieve;

import index.domain.Dictionary;
import index.domain.Document;
import index.domain.MyInteger;
import index.domain.PostingList;
import index.domain.Token;
import index.util.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import retrieve.Measure;
import retrieve.Rank;
import retrieve.util.*;
/**
 * @author Cao Yixuan
 */
public class Search {
	
	public static double N; // # document
	
	
	//加载索引器，包括词典，文档的长度和所对应的docNo
	public static String indexPath;
	public static String docPath;
	
	public static Dictionary dic;
	public static int[] docLengths;
	public static String[] docNoByDocId;
	public static ArrayList<Double> docVectorLengths;
	public static ArrayList<Double> docPageRanks;
	public static ArrayList<Double> docUrlDepths;
	
	public static void main(String[] args) throws IOException{
		initialize();
		ArrayList<DocScoreList> docs = SearchFor("Algorithm Design");
		docs = Rank.rank(docs);
		for (int i=0; i<10; i++){
			DocScoreList doc = docs.get(i);
			System.out.println(docNoByDocId[doc.docid] + ": " + doc.scores.toString());
			Document docObj = Document.getDocument(docNoByDocId[doc.docid], docPath);
			System.out.println(docObj.content);
			String title = docObj.title;
			System.out.println("-----------------\n---------------------");
		}
		System.out.println(docNoByDocId[docs.get(docs.size()-1).docid] + ": " + docs.get(docs.size()-1).scores.toString());
		// System.out.print(docs.toString());
	}
	
	public static ArrayList<DocScoreList> SearchFor(String query) throws IOException{
		// Given a query, return all possible relevant documents and their scores.
		ArrayList<TermFreq> queryTermsFreq = new ArrayList<TermFreq>(); // stemming and lemmatization the query
		ArrayList<PostingList> PLs = new ArrayList<PostingList>(); // all the posting lists
		retrivePostingLists(query, queryTermsFreq, PLs);
		return Measure.ComputeScores(queryTermsFreq, PLs);
	}
	
	
	
	public static void retrivePostingLists(String query, ArrayList<TermFreq> queryTermsFreq, ArrayList<PostingList> PLs) throws IOException{
		HashMap<String, Integer> termFreqDict = new HashMap<String, Integer>(); // count the term frequency while processing
		MyInteger index = new MyInteger(0);
		Token token;
		// termFreqDict count the term frequency
		// queryTerms has the right order but not the right freq
		while((token=Tokenizer.nextToken(query, 0, index)) != null){
			token = token.preprocess();
			if(token.term.equals("")) continue; //词条，停用词
			String term = token.term;
			if(termFreqDict.containsKey(term)){
				termFreqDict.replace(term, termFreqDict.get(term)+1);
			}else{
				PostingList pl = dic.getPL(Token.preprocess(term.trim()));
				if(pl == null){
					System.out.println("term \"" + term + "\" do not has posting list.");
				}else{
					termFreqDict.put(term, 1);
					pl = PostingList.decode(pl.c_pl, new MyInteger(0));
					TermFreq queryTermFreq = new TermFreq(term, 1);
					queryTermsFreq.add(queryTermFreq);
					PLs.add(pl);
				}
			}
		}
		// correct the frequency of queryTerms
		for (int i=0; i<queryTermsFreq.size(); i++){
			queryTermsFreq.get(i).freq = termFreqDict.get(queryTermsFreq.get(i).term);
		}
	}
	
	public static void initialize() throws IOException{
		indexPath = "i:\\kuaipan\\graduateCourses\\IR\\program\\data\\index";
		docPath = "i:\\kuaipan\\graduateCourses\\IR\\program\\data\\WT10G\\WT10G_copy";
		dic = Dictionary.load(
					new File(indexPath + "/dictionary"), 
					new File(indexPath + "/postinglist"));
		System.out.println("Dictionary loaded");
		docLengths = Document.loadDocLengths(new File(indexPath + "/docLengths"));
		docNoByDocId = Document.loadDocNoByDocId(new File(indexPath + "/docNoByDocId"));
		System.out.println("Document information loaded");
		N = docNoByDocId.length;
		
		Measure.initialize(dic, docLengths, docNoByDocId, N);
	}
	
	
	
}
