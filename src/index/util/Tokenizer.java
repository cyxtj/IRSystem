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
		int cnt = 0; //term�����ָ���
		while(startIdx.value < doc.length()){
			char c = doc.charAt(startIdx.value++);
			if(isLetter(c)){ //��ĸ
				term.append(lowcase(c));
			}else if(isDigital(c)){ //����
				term.append(c);
				cnt++;
			}else{ //�����ַ���ͳһ�����ָ���
				if(term.length() > 0){ //����һ���ַ����������Ǵ���
					if(cnt==term.length()){ //ȫ�����֣�����Ϊ����
						term = new StringBuilder();
						cnt  = 0;
					}else break; //�ҵ�һ������
				}
			}
		}
		if(term.length() > 0){ //����һ���ַ����������Ǵ���
			if(cnt==term.length()){ //ȫ�����֣�����Ϊ����
				term = new StringBuilder();
				cnt  = 0;
			}
		}
		if(term.length()==0) return null;
		return new Token(term.toString(), docid);
	}
}
