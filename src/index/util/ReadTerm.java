package index.util;

import java.io.BufferedInputStream;
import java.io.IOException;

public class ReadTerm {
	private int SIZE = 1024000;
	private BufferedInputStream dicIn;
	private byte[] cur;
	private int index;
	private int length;
	public ReadTerm(BufferedInputStream dicIn) throws IOException{
		this.dicIn = dicIn;
		cur = new byte[SIZE];
		index = 0;
		length = dicIn.read(cur);
	}
	
	public String nextTerm() throws IOException{
		StringBuilder tmp = new StringBuilder();
		while(index < length){
			if(cur[index] == ' ') {index++; return tmp.toString();}
			tmp.append((char)(cur[index++]));
		}
		if(index == length){ //可能还没读完
			index = 0;
			length = dicIn.read(cur);
			while(index < length){
				if(cur[index] == ' ') {index++; return tmp.toString();}
				tmp.append((char)(cur[index++]));
			}
		}
		return null;
	}
}
