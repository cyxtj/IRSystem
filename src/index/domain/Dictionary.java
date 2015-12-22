package index.domain;

import index.util.ReadPL;
import index.util.ReadTerm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;


public class Dictionary {
	private HashMap<String , PostingList> dic; //从term到term attribute的词典
	
	public Dictionary(){
		dic = new HashMap<String , PostingList>();
	}
	
	public boolean addTerm(String key, PostingList pl){
		dic.put(key, pl);
		return true;
	}
	
	public boolean addTerm(Token token){
		PostingItem pi = new PostingItem(token.docid, 1);
		PostingList pl = new PostingList();
		pl.addPostingItem(pi);
		dic.put(token.term, pl);
		return true;
	}
	
	public boolean hasTerm(String term){
		return dic.get(term) != null;
	}
	
	public PostingList getPL(String term){
		return dic.get(term);
	}
	
	public Set<String> getKeySet(){
		return dic.keySet();
	}
	
	/**
	 * 压缩并存储
	 */
	public static boolean store(Dictionary dic, File dicfile, File plfile) throws IOException{
		Set<String> keySet = dic.getKeySet();
		ArrayList<String> keyList = new ArrayList<String>(); 
		for(String key : keySet) keyList.add(key);
		Collections.sort(keyList);
		ArrayList<Byte> tmp;
				
		BufferedOutputStream dicOut = new BufferedOutputStream(new FileOutputStream(dicfile));
		BufferedOutputStream plOut = new BufferedOutputStream(new FileOutputStream(plfile));
		
		for(String term : keyList){
			PostingList pl = dic.getPL(term);
			dicOut.write((term+" ").getBytes());
			tmp = PostingList.encode(pl);
			for(Byte b : tmp) plOut.write(b);
		}
		dicOut.flush();
		plOut.flush();
		dicOut.close();
		plOut.close();
		return true;
	}
	
	/**
	 * 加载并解压词典和倒排记录表
	 */
	public static Dictionary load(File dicfile, File plfile) throws IOException{
		Dictionary dic = new Dictionary();
		BufferedInputStream dicIn = new BufferedInputStream(new FileInputStream(dicfile)); 
		BufferedInputStream plIn  = new BufferedInputStream(new FileInputStream(plfile)); 
		String term;
		ReadTerm rt = new ReadTerm(dicIn);
		ReadPL rpl = new ReadPL(plIn);
		while((term=rt.nextTerm()) != null){
			PostingList pl = rpl.nextPL();
			pl.list = null;
			dic.addTerm(term, pl);
		}
		dicIn.close();
		plIn.close();
		return dic;
	}
	
}
