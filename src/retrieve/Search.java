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
import java.util.Set;

import retrieve.Measure;
import retrieve.util.*;
/**
 * @author Cao Yixuan
 */
public class Search {
	
	public static double N; // # document
	
	
	//加载索引器，包括词典，文档的长度和所对应的docNo
	public String indexPath;
	public String docPath;
	
	public Dictionary contentDic;
	public Dictionary titleDic;
	public String[] docNoByDocId;
	public Measure M;
	
	public Search(String indexPath) throws IOException{
		this.indexPath = indexPath;
		initialize();
	}
	
	public ArrayList<DocScoreList> SearchFor(String query) throws IOException{
		// Given a query, return all possible relevant documents and their scores.
		ArrayList<TermFreq> queryTermsFreqContent = new ArrayList<TermFreq>();
		ArrayList<TermFreq> queryTermsFreqTitle = new ArrayList<TermFreq>();
		
		HashMap<String, Integer> queryTermFreqDict = new HashMap<String, Integer>();
		queryTermFrequency(query, queryTermFreqDict);
		
		ArrayList<PostingList> contentPLs = new ArrayList<PostingList>(); // all the posting lists
		ArrayList<PostingList> titlePLs = new ArrayList<PostingList>(); 
		retrivePostingLists(queryTermFreqDict, queryTermsFreqContent, contentPLs);
		retriveTitlePostingLists(queryTermFreqDict, queryTermsFreqTitle, titlePLs);
		
		ArrayList<DocScoreList> docsScores = M.computeScores(contentPLs, queryTermsFreqContent, titlePLs, queryTermsFreqTitle);
		sort(docsScores);
		return docsScores;
	}
	
	public void queryTermFrequency(String query, HashMap<String, Integer> termFreqDict) throws IOException{
		//HashMap<String, Integer> termFreqDict = new HashMap<String, Integer>(); // count the term frequency while processing
		MyInteger index = new MyInteger(0);
		Token token;
		// count the frequency of each term in query
		while((token=Tokenizer.nextToken(query, 0, index)) != null){
			token = token.preprocess();
			if(token.term.equals("")) continue; //词条，停用词
			String term = token.term;
			System.out.print(" -" + term + "- ");
			if(termFreqDict.containsKey(term)){
				termFreqDict.replace(term, termFreqDict.get(term)+1);
			}else{
				termFreqDict.put(term, 1);
			}
		}
	}
	
	public void retrivePostingLists(HashMap<String, Integer> termFreqDict, ArrayList<TermFreq> queryTermsFreq, ArrayList<PostingList> PLs) throws IOException{
	
		// retrieve postinglists
		Set<String> terms = termFreqDict.keySet();
		for(String term: terms){
			PostingList pl = contentDic.getPL(Token.preprocess(term.trim()));
			if(pl == null){
				System.out.println("-no \"" + term + "\" in content PL.-");
			}else{
				pl = PostingList.decode(pl.c_pl, new MyInteger(0));
				PLs.add(pl);
				TermFreq queryTermFreq = new TermFreq(term, termFreqDict.get(term));
				queryTermsFreq.add(queryTermFreq);
			}
		}
		
	}
	
	
	public void retriveTitlePostingLists(HashMap<String, Integer> termFreqDict, ArrayList<TermFreq> queryTermsFreq, ArrayList<PostingList> PLs) throws IOException{
		// retrieve postinglists
		Set<String> terms = termFreqDict.keySet();
		for(String term: terms){
			PostingList pl = titleDic.getPL(Token.preprocess(term.trim()));
			if(pl == null){
				System.out.println("-no \"" + term + "\" in title PL.-");
			}else{
				pl = PostingList.decode(pl.c_pl, new MyInteger(0));
				PLs.add(pl);
				TermFreq queryTermFreq = new TermFreq(term, termFreqDict.get(term));
				queryTermsFreq.add(queryTermFreq);
			}
		}
		
	}
	
	public void initialize() throws IOException{
		//indexPath = "i:\\kuaipan\\graduateCourses\\IR\\program\\data\\index";
		contentDic = Dictionary.load(
					new File(indexPath + "/dictionary"), 
					new File(indexPath + "/postinglist"));
		titleDic = Dictionary.load(
				new File(indexPath + "/title/dictionary"), 
				new File(indexPath + "/title/postinglist"));
		System.out.println("Dictionary loaded");
		docNoByDocId = Document.loadDocNoByDocId(new File(indexPath + "/docNoByDocId"));
		M = new Measure(indexPath, docNoByDocId);
	}
	
	public void sort(ArrayList<DocScoreList> docsScores){
		DocScoreListComparator dslc = new DocScoreListComparator();
		docsScores.sort(dslc);
	}
	
}
