package index.util;

import index.domain.MyInteger;
import index.domain.PostingList;

import java.io.BufferedInputStream;
import java.io.IOException;


public class ReadPL {
	private int SIZE = 1024000;
	private BufferedInputStream plIn;
	private byte[] cur;
	private MyInteger index;
	private int preIndex;
	public ReadPL(BufferedInputStream plIn) throws IOException{
		this.plIn = plIn;
		cur = new byte[SIZE];
		index = new MyInteger(0);
		plIn.read(cur);
	}
	
	public PostingList nextPL() throws IOException{ //解码一个倒排记录表，并返回
		preIndex = index.value;
		PostingList pl = PostingList.decode(cur, index);
		while(pl == null){
			int start = cur.length - preIndex;
			if(preIndex == 0){ //有超大的倒排记录表
				SIZE *= 10;
				byte[] t = new byte[SIZE];
				for(int i=0; i<cur.length; i++) 
					t[i] = cur[i];
				cur = t;
			}else{
				for(int i=preIndex; i<cur.length; i++) 
					cur[i-preIndex] = cur[i];
			}
			plIn.read(cur, start, cur.length-start);
			preIndex = index.value = 0;
			pl = PostingList.decode(cur, index);
		}
		byte[] c_pl = new byte[index.value-preIndex];
		for(int i=preIndex; i<index.value; i++) c_pl[i-preIndex] = cur[i];
		pl.c_pl = c_pl;
		return pl;
	}
}
