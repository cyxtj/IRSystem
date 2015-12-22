package index.domain;

import index.util.ReadPL;
import index.util.ReadTerm;
import index.util.TrecParser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.GZIPInputStream;


public class Document {
	public int docid;
	public String docno;
	public String title;
	public String content;
	public String webpage;
	
	public Document(String docNo, String title, String content, String webpage) {
		this.docno = docNo;
		this.title = title;
		this.content = content;
		this.webpage = webpage;
	}

	/**
	 * �洢ÿ���ĵ�����
	 **/
	public static boolean storeDocLengths(String filename, int docNum) throws IOException{
		BufferedInputStream dicIn = new BufferedInputStream(new FileInputStream(filename+"/dictionary")); 
		BufferedInputStream plIn  = new BufferedInputStream(new FileInputStream(filename+"/postinglist")); 
		BufferedOutputStream dlOut  = new BufferedOutputStream(new FileOutputStream(new File(filename+"/docLengths"))); 
		ReadTerm rt = new ReadTerm(dicIn);
		ReadPL rpl = new ReadPL(plIn);
		int[] docLengths = new int[docNum];
		for(int i=0; i<docNum; i++) docLengths[i] = 0;
		while(rt.nextTerm() != null){
			PostingList pl = rpl.nextPL();
			for(int i=0; i<pl.getSize(); i++) docLengths[pl.getItem(i).docid] += pl.getItem(i).tf;
		}
		for(int i=0; i<docNum; i++) dlOut.write(toByteArray(docLengths[i]));
		dlOut.flush();
		dlOut.close();
		dicIn.close();
		plIn.close();
		return true;
	}
	
	/**
	 * ����ÿ���ĵ��ĳ���
	 * @throws IOException 
	 **/
	public static int[] loadDocLengths(File dlfile) throws IOException{
		BufferedInputStream plIn  = new BufferedInputStream(new FileInputStream(dlfile)); 
		byte[] tmp = new byte[4];
		ArrayList<Integer> ls = new ArrayList<Integer>();
		while(plIn.read(tmp) != -1)
			ls.add(toInt(tmp));
		int[] lengths = new int[ls.size()];
		for(int i=0; i<ls.size(); i++) lengths[i] = ls.get(i);
		plIn.close();
		return lengths;
	}
	
	/**
	 * docid��Ӧ��DOCNO
	 * @throws IOException 
	 **/
	public static String[] loadDocNoByDocId(File file) throws IOException{
		BufferedReader in  = new BufferedReader(new FileReader(file)); 
		String line;
		ArrayList<String> list = new ArrayList<String>(); 
		while((line=in.readLine()) != null && !line.equals("")) list.add(line);
		String[] docnos = new String[list.size()];
		for(int i=0; i<list.size(); i++) docnos[i] = list.get(i);
		in.close();
		return docnos;
	}
	
	/**
	 * ͨ��DOCNO��ȡ�ĵ�,���WT10G�����ĵ��ṹ,path�����Ͽ��·��
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 **/
	public static Document getDocument(String docNo, String path) throws IOException{
		String[] tmp = docNo.split("-");
		path = path + "/" + tmp[0] + "/" + tmp[1];
		File file = new File(path);
		if(!file.exists()){
			path += ".gz";
			file = new File(path);
			if(!file.exists()) {System.out.println("no such file");return null;}
			else file = Document.unzip(file);
		}
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Document doc;
		while((doc=TrecParser.next_doc(reader)) != null){
			if(doc.docno.equals(docNo)){ 
				reader.close();
				return doc;
			}
		}
		reader.close();
		return null;
	}
	
	/**
	 * ��ѹgz�ļ�����ʱ�ļ�������gz�ļ��ͷ���ԭ�ļ�
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 **/
	public static File unzip(File file) throws FileNotFoundException, IOException{
		System.out.print("���ڹ����ļ���" + file.getAbsolutePath()+" ");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//�������ڸ�ʽ
		System.out.println(df.format(new Date()));// new Date()Ϊ��ȡ��ǰϵͳʱ��

		String path = file.getAbsolutePath();
		if(!path.endsWith(".gz")) return file;
		System.out.println("���ڽ�ѹ��" + path);
		File tmp = File.createTempFile("pattern", "suffix");
		GZIPInputStream gzi = new GZIPInputStream(new FileInputStream(path)); 
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmp.getAbsolutePath())); 
		int b;
		do {
			b = gzi.read(); 
			if (b==-1) break; 
			bos.write(b); 
		} while (true);
		gzi.close(); 
		bos.close();
		System.out.println("��ѹ��ɣ����ڹ���");
		return tmp;
	}
	
	public static ArrayList<Document> parserFile(File file) throws IOException{
		ArrayList<Document> list = new ArrayList<Document>();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Document doc;
		while((doc=TrecParser.next_doc(reader)) != null){
			list.add(doc);
		}
		reader.close();
		return list;
	}
	
	public static byte[] toByteArray(int iSource) {
	    byte[] bLocalArr = new byte[4];
	    for (int i=0; i<4; i++) {
	        bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
	    }
	    return bLocalArr;
	}
	
	public static int toInt(byte[] bRefArr) {
	    int iOutcome = 0;
	    byte bLoop;
	    for (int i = 0; i < bRefArr.length; i++) {
	        bLoop = bRefArr[i];
	        iOutcome += (bLoop & 0xFF) << (8 * i);
	    }
	    return iOutcome;
	}
}
