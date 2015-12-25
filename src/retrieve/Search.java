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
		ArrayList<TermFreq> queryTermsFreq = new ArrayList<TermFreq>(); // stemming and lemmatization the query
		ArrayList<PostingList> contentPLs = new ArrayList<PostingList>(); // all the posting lists
		ArrayList<PostingList> titlePLs = new ArrayList<PostingList>(); 
		retrivePostingLists(query, queryTermsFreq, contentPLs);
		retriveTitlePostingLists(query, queryTermsFreq, titlePLs);
		ArrayList<DocScoreList> docsScores = M.computeScores(queryTermsFreq, contentPLs, titlePLs);
		sort(docsScores);
		return docsScores;
	}
	
	
	
	public void retrivePostingLists(String query, ArrayList<TermFreq> queryTermsFreq, ArrayList<PostingList> PLs) throws IOException{
		HashMap<String, Integer> termFreqDict = new HashMap<String, Integer>(); // count the term frequency while processing
		MyInteger index = new MyInteger(0);
		Token token;
		// termFreqDict count the term frequency
		// queryTerms has the right order but not the right freq
		while((token=Tokenizer.nextToken(query, 0, index)) != null){
			token = token.preprocess();
			if(token.term.equals("")) continue; //词条，停用词
			String term = token.term;
			System.out.print(term);
			if(termFreqDict.containsKey(term)){
				termFreqDict.replace(term, termFreqDict.get(term)+1);
			}else{
				PostingList pl = contentDic.getPL(Token.preprocess(term.trim()));
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
	public void retriveTitlePostingLists(String query, ArrayList<TermFreq> queryTermsFreq, ArrayList<PostingList> PLs) throws IOException{
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
				PostingList pl = titleDic.getPL(Token.preprocess(term.trim()));
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
