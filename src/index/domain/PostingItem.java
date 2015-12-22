package index.domain;

public class PostingItem {
	public int docid;
	public int tf;
	
	public PostingItem(){}
	
	public PostingItem(int docid, int tf){
		this.docid = docid;
		this.tf = tf;
	}
}