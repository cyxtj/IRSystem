package retrieve.util;

import java.util.ArrayList;

public class DocTerms {
	private int docid;
	private int doc_length;
	private ArrayList<TermTF> term_tf;
	public DocTerms(int docid){
		this.setDocid(docid);
	}
	public void addTermTF(TermTF term_tf){
		this.term_tf.add(term_tf);
	}
	
	public ArrayList<TermTF> getTermTF(){
		return term_tf;
	}
	public int getDocid() {
		return docid;
	}
	public void setDocid(int docid) {
		this.docid = docid;
	}

	public int getDoc_length() {
		return doc_length;
	}
	public void setDoc_length(int doc_length) {
		this.doc_length = doc_length;
	}







	public class TermTF{
		public int term;
		public double tf;
		public TermTF(int term, double tf){
			this.term = term;
			this.tf = tf;
		}
	}
}
