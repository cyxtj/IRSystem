package index.util;

import index.domain.Document;
import index.domain.MyInteger;
import index.domain.Token;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class TokenStream{
	private File[] fileList;
	private int curFileIdx;
	private ArrayList<Document> docList;
	private int curDocId;
	private int curDocIdx;
	private String curDoc;
	private MyInteger curDocIdxInDoc;
	private Token curToken = null;
	private BufferedWriter docNoByDocId = null;
	private File unziped = null;
	private String flag = null; //词条化content或者title
	
	public TokenStream(String foldername, String flag) throws IOException{
		File doc = new File("./docNoByDocId");
		if(doc.exists() && doc.isFile()) doc.delete();
		docNoByDocId = new BufferedWriter(new FileWriter(doc));
		
		this.flag = flag;
		fileList = getAllFile(foldername);
		curDocIdxInDoc = new MyInteger(0);
		curFileIdx = curDocIdx = curDocId = 0;
		
		unziped = Document.unzip(fileList[curFileIdx]);
		docList = Document.parserFile(unziped);
		System.out.println("该文件文档数："+docList.size());
		if(flag.equals("content")) curDoc = docList.get(curDocIdx).content;
		else curDoc = docList.get(curDocIdx).title;
		docNoByDocId.write(docList.get(curDocIdx).docno+"\n");
		docNoByDocId.flush();
		next();
	}
	
	/**
	 * 返回文件夹中的所有文件(递归)
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 **/
	public File[] getAllFile(String foldername) throws FileNotFoundException, IOException{
		ArrayList<File> list = new ArrayList<File>();
		File file = new File(foldername);
		searchFolder(file, list);
		File[] fileList = new File[list.size()];
		for(int i=0; i<list.size(); i++) fileList[i] = list.get(i);
		return fileList;
	}
	
	/**
	 * 递归遍历folder中的所有文件，将所有文件放到list中
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 **/
	public void searchFolder(File folder, ArrayList<File> list) throws FileNotFoundException, IOException{
		File[] tempList = folder.listFiles();
		for(int i=0; i<tempList.length; i++){
			if(tempList[i].isFile()){
				list.add(tempList[i]);
			}else if(tempList[i].isDirectory()){
				searchFolder(tempList[i], list);
			}
		}
	}
	
	public boolean close(){
		try {
			docNoByDocId.flush();
			docNoByDocId.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean nextFile() throws IOException{
		if(!unziped.getAbsoluteFile().equals(fileList[curFileIdx])) unziped.delete();
		curFileIdx++;
		if(curFileIdx >= fileList.length) return false;
		curDocIdx = 0;
		unziped = Document.unzip(fileList[curFileIdx]);
		docList = Document.parserFile(unziped);
		System.out.println("该文件文档数："+docList.size());
		return true;
	}
	
	public boolean nextDoc() throws IOException{
		curDocIdx++;
		if(curDocIdx >= docList.size() && nextFile()==false) return false;
		if(flag.equals("content")) curDoc = docList.get(curDocIdx).content;
		else curDoc = docList.get(curDocIdx).title;
		curDocId++;
		docNoByDocId.write(docList.get(curDocIdx).docno+"\n");
		docNoByDocId.flush();
		curDocIdxInDoc.value = 0;
		return true;
	}
	
	public Token next() throws IOException {
		Token ret = curToken;
		curToken = null;
		while(curToken == null){
			if(curDocIdxInDoc.value == curDoc.length() && nextDoc()==false) return ret; //已经没有文档可以词条化
			curToken = Tokenizer.nextToken(curDoc, curDocId, curDocIdxInDoc);
		}
		return ret;
	}

	public boolean hasNext() {
		return curToken!=null;
	}

}
