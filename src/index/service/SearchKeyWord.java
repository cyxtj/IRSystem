package index.service;

import index.domain.Dictionary;
import index.domain.Document;
import index.domain.MyInteger;
import index.domain.PostingItem;
import index.domain.PostingList;
import index.domain.Token;
import index.util.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;



public class SearchKeyWord {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws IOException{
		//�����������������ʵ䣬�ĵ��ĳ��Ⱥ�����Ӧ��docNo
		String indexPath = "./WT10G";
		String docPath = "C:\\Users\\adama\\Desktop\\���ƴ�\\��ҵ\\�ִ���Ϣ����\\WT10G";
		if(args.length > 1) indexPath = args[0];
		Dictionary dic = Dictionary.load(
				new File(indexPath + "/dictionary"), 
				new File(indexPath + "/postinglist"));
		Dictionary titleDic = Dictionary.load(
				new File(indexPath + "/title/dictionary"), 
				new File(indexPath + "/title/postinglist"));
		int[] docLengths = Document.loadDocLengths(new File(indexPath + "/docLengths"));
		int[] titleLengths = Document.loadDocLengths(new File(indexPath + "/title/docLengths"));
		double avg_l = 0; for(int length : docLengths) avg_l += length; avg_l /= docLengths.length;
		String[] docNoByDocId = Document.loadDocNoByDocId(new File(indexPath + "/docNoByDocId"));
		
		System.out.println("�ĵ�ƽ�����ȣ�" + avg_l);
		
		//ͨ��docid����Ӧ��docNo�õ��ĵ�
		int docid = 1542;
		Document doc = Document.getDocument(docNoByDocId[docid], docPath); //��ȡ��һƪ�ĵ�
		System.out.println(doc.docno); //docno
		System.out.println(doc.title); //����
		System.out.println(doc.content); //����
		System.out.println(doc.webpage); //��ҳ����
		
		//���ݹؼ��ʣ�������ż�¼��
		Scanner scan = new Scanner(System.in);
		while(true){
			System.out.print("Please input a query:");
			String query = scan.nextLine();
			MyInteger index = new MyInteger(0);
			Token token;
			while((token=Tokenizer.nextToken(query, 0, index)) != null){
				token = token.preprocess();
				if(token.term.equals("")) continue; //������ͣ�ô�
				String term = token.term;
				//content���ż�¼��
				PostingList pl = dic.getPL(term);
				if(pl == null){
					System.out.println("content dictionary : no " + term);
				}else{
					pl = PostingList.decode(pl.c_pl, new MyInteger(0));
					System.out.print("content dictionary : ("+term+","+pl.getDF()+"):");
					for(int i=0; i<Math.min(pl.getSize(),10); i++){
						PostingItem item = pl.getItem(i);
						int id = item.docid;
						System.out.print("->("+id+","+docNoByDocId[id]+","+item.tf+","+docLengths[id]+")");
					}
					System.out.println("");
				}
				//title���ż�¼��
				pl = titleDic.getPL(term);
				if(pl == null){
					System.out.println("title dictionary : no " + term);
				}else{
					pl = PostingList.decode(pl.c_pl, new MyInteger(0));
					System.out.print("title dictionary : ("+term+","+pl.getDF()+"):");
					for(int i=0; i<Math.min(pl.getSize(),10); i++){
						PostingItem item = pl.getItem(i);
						int id = item.docid;
						System.out.print("->("+id+","+docNoByDocId[id]+","+item.tf+","+titleLengths[id]+")");
					}
					System.out.println("");
				}
			}
		}
	}

}
