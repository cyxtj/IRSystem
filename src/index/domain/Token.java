package index.domain;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class Token {
	public String term;
	public int docid;
	
	public Token(String term, int docid) throws IOException {
		this.term = term;
		this.docid = docid;
	}
	
	public Token preprocess() throws IOException {
	     term = preprocess(term);
	     return this;
	}
	
	public static String preprocess(String term) throws IOException{
		 String preprocessedTerm = "";  
	     Analyzer analyzer = new StandardAnalyzer();  
	     TokenStream ts = analyzer.tokenStream(null, term);  
	     ts = new PorterStemFilter(ts);  
	     CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);  
	              
	     ts.reset();
	     if(ts.incrementToken())
	         preprocessedTerm = charTermAttribute.toString();  
	     ts.end();  
	     ts.close();
	     analyzer.close();
	     return preprocessedTerm;
	}
}
