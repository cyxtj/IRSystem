package index.util;
import index.domain.Document;

import java.io.BufferedReader;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;


public class TrecParser {
	public static Document next_doc(BufferedReader file) throws IOException{
		String line;
		while((line=file.readLine()) != null) {
        	if(line.contains("<DOCNO>")) {
        		String docNo = line.replace("<DOCNO>", "").replace("</DOCNO>", "");
        		StringBuilder content = new StringBuilder();
        		String tmp = file.readLine();
        		if(tmp == null) continue; //空文本
        		if(tmp.contains("<DOCOLDNO>")) tmp = file.readLine(); //删掉DOCOLDNO
        		if(tmp == null) continue; //空文本
        		if(tmp.contains("<DOCHDR>")){ //删掉DOCHDR
        			while((tmp=file.readLine())!=null && !tmp.equals("</DOCHDR>"));
        			tmp = file.readLine();
        		}
        		while(tmp!=null && !tmp.equals("</DOC>")) { //读取CONTENT
        			content.append(tmp);
        			tmp = file.readLine();
        		}
        		if(content.length()==0) continue; //空文本
        		//用jsoup解析文档
        		String webpage = content.toString();
        		org.jsoup.nodes.Document doc = Jsoup.parse(webpage);
        		Elements es = doc.getElementsByTag("title");
        		if(es.size() == 0) continue; //无title文本
        		String title = es.get(0).text().trim();//System.out.println(title);
        		es.get(0).remove(); //System.out.println(doc.text());
        		return new Document(docNo, title, doc.text(), webpage);
        	}
        }
		return null;
	}
	
}
