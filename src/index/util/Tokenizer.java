package index.util;

import index.domain.MyInteger;
import index.domain.Token;

import java.io.IOException;


public class Tokenizer {
	
	public static boolean isLetter(char c){
		return c>='a' && c<='z' || c>='A' && c<='Z';
	}
	
	public static boolean isDigital(char c){
		return c>='0' && c<='9';
	}
	
	public static char lowcase(char c){
		if(c>='A' && c<='Z') return (char) (c - 'A' + 'a');
		else return c;
	}
	
	public static Token nextToken(String doc, int docid, MyInteger startIdx) throws IOException{
		StringBuilder term = new StringBuilder();
		int cnt = 0; //term中数字个数
		while(startIdx.value < doc.length()){
			char c = doc.charAt(startIdx.value++);
			if(isLetter(c)){ //字母
				term.append(lowcase(c));
			}else if(isDigital(c)){ //数字
				term.append(c);
				cnt++;
			}else{ //其他字符，统一看做分隔符
				if(term.length() > 0){ //遇到一个字符串，可能是词条
					if(cnt==term.length()){ //全是数字，不做为词条
						term = new StringBuilder();
						cnt  = 0;
					}else break; //找到一个词条
				}
			}
		}
		if(term.length() > 0){ //遇到一个字符串，可能是词条
			if(cnt==term.length()){ //全是数字，不做为词条
				term = new StringBuilder();
				cnt  = 0;
			}
		}
		if(term.length()==0) return null;
		return new Token(term.toString(), docid);
	}
}
