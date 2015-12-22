package index.service;

import index.domain.Dictionary;
import index.domain.Document;
import index.domain.PostingItem;
import index.domain.PostingList;
import index.domain.Token;
import index.util.FileOperation;
import index.util.ReadPL;
import index.util.ReadTerm;
import index.util.TokenStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class SPIMI {
	private int termNum = 0;
	private int docNum = 0;
	private int tokenNum = 0;
	
	/**
	 * name是语料名称，ts是语料的词条流
	 */
	public SPIMI(TokenStream ts, String name) throws IOException{
		int cnt = 0;
		String filename = "./" + name;
		FileOperation.createFolder(new File(filename));
		while(ts.hasNext()){
			SPIMI_Invert(ts, filename + "/" + cnt);
			cnt++;
		}
		ts.close();
		mergeBlock(filename, cnt);
		System.out.println("词项数量："+termNum);
		System.out.println("文档数量："+docNum);
		System.out.println("词条数量："+tokenNum);
		System.out.println("文档平均长度："+(1.0*tokenNum/docNum));
		Document.storeDocLengths(filename, docNum);
		new File("./docNoByDocId").renameTo(new File(filename+"/docNoByDocId"));
	}
	
	/**
	 * 在有限内存下给部分词条做倒排索引，存到文件filename，被多次调用
	 */
	private boolean SPIMI_Invert(TokenStream ts, String filename) throws IOException{
		Dictionary dic = new Dictionary();
		int cc=0;//Runtime rt = Runtime.getRuntime();
		while(cc++<30000000 && ts.hasNext()){//while(rt.freeMemory() > 1024000 && ts.hasNext()){ //java虚拟机中空闲内存小于等于1MB时停止
			Token token = ts.next();
			token = token.preprocess();
			if(token.term.equals("")) continue;
			//System.out.println(token.docid+" "+token.term);
			tokenNum++; //记录词条总数
			if(token.docid+1 > docNum) docNum = token.docid+1; //记录文档总数
			if(!dic.hasTerm(token.term)){ 
				dic.addTerm(token);
			}else {
				PostingList pl = dic.getPL(token.term);
				if(pl.lastItem().docid == token.docid){
					pl.lastItem().tf++;
				}else {
					pl.addPostingItem(new PostingItem(token.docid, 1));
				}
			}
		}
		writeBlockToDisk(dic, filename);
		return true;
	}
	
	/**
	 * 将块的词典和倒排记录表存到硬盘
	 */
	private boolean writeBlockToDisk(Dictionary dic, String filename) throws IOException{
		File dicfile = new File(filename + "_dic");
		File plfile = new File(filename + "_pl");
		Dictionary.store(dic, dicfile, plfile);
		termNum = dic.getKeySet().size(); //如果只有一个block，则这是termNum
		return true;
	}
	
	/**
	 * 将所有块合并成一个倒排索引
	 */
	private void mergeBlock(String filename, int cnt) throws IOException {
		ArrayList<Integer> fileIds = new ArrayList<Integer>();
		for(int i=0; i<cnt; i++) fileIds.add(i);
		while(fileIds.size() != 1){
			for(int i=0; i+1<fileIds.size(); i++){
				int id1 = fileIds.get(i);
				int id2 = fileIds.get(i+1);
				System.out.println("合并文件"+id1+" "+id2);
				termNum = mergeFile(filename, id1, id2);
				fileIds.remove(i+1);
			}
		}
		new File(filename+"/"+fileIds.get(0)+"_dic").renameTo(new File(filename+"/dictionary"));
		new File(filename+"/"+fileIds.get(0)+"_pl").renameTo(new File(filename+"/postinglist"));
	}
	
	/**
	 * 合并两个倒排记录表文件
	 * @throws IOException 
	 **/
	public int mergeFile(String filename, int id1, int id2) throws IOException{
		int termNum = 0; //两个文档词项数
		BufferedInputStream dicIn1 = new BufferedInputStream(new FileInputStream(new File(filename+"/"+id1+"_dic"))); 
		BufferedInputStream plIn1  = new BufferedInputStream(new FileInputStream(new File(filename+"/"+id1+"_pl")));
		BufferedInputStream dicIn2 = new BufferedInputStream(new FileInputStream(new File(filename+"/"+id2+"_dic"))); 
		BufferedInputStream plIn2  = new BufferedInputStream(new FileInputStream(new File(filename+"/"+id2+"_pl")));
		BufferedOutputStream dicOut = new BufferedOutputStream(new FileOutputStream(new File(filename+"/"+id1+"_dic_tmp")));
		BufferedOutputStream plOut = new BufferedOutputStream(new FileOutputStream(new File(filename+"/"+id1+"_pl_tmp")));
		
		ReadTerm rt1 = new ReadTerm(dicIn1);
		ReadPL rpl1 = new ReadPL(plIn1);
		ReadTerm rt2 = new ReadTerm(dicIn2);
		ReadPL rpl2 = new ReadPL(plIn2);

		ArrayList<Byte> tmp;
		String term1 = rt1.nextTerm();
		String term2 = rt2.nextTerm();
		while(term1!=null && term2!=null){ //两个块都还有没合并的
			//System.out.println(term1);System.out.println(term2);
			if(term1.equals(term2)){ //同一个term
				PostingList pl1 = rpl1.nextPL();
				PostingList pl2 = rpl2.nextPL();
				pl1.append(pl2);
				dicOut.write((term1+" ").getBytes());
				tmp = PostingList.encode(pl1);
				for(Byte b : tmp) plOut.write(b);
				term1 = rt1.nextTerm();
				term2 = rt2.nextTerm();
			}else if(term1.compareTo(term2) < 0){ //第一个块的当前term比第二个块小
				PostingList pl1 = rpl1.nextPL();
				dicOut.write((term1+" ").getBytes());
				plOut.write(pl1.c_pl);
				term1 = rt1.nextTerm();
			}else{ //第二个块的当前term比第一个块小
				PostingList pl2 = rpl2.nextPL();
				dicOut.write((term2+" ").getBytes());
				plOut.write(pl2.c_pl);
				term2 = rt2.nextTerm();
			}
			termNum++;
		}
		while(term1 != null){ //第二块已合并完，第一块还有剩余
			PostingList pl1 = rpl1.nextPL();
			dicOut.write((term1+" ").getBytes());
			plOut.write(pl1.c_pl);
			term1 = rt1.nextTerm();
			termNum++;
		}
		while(term2 != null){ //第一块已合并完，第二块还有剩余
			PostingList pl2 = rpl2.nextPL();
			dicOut.write((term2+" ").getBytes());
			plOut.write(pl2.c_pl);
			term2 = rt2.nextTerm();
			termNum++;
		}
		dicOut.flush();
		plOut.flush();
		dicOut.close();
		plOut.close();
		dicIn1.close();
		plIn1.close();
		dicIn2.close();
		plIn2.close();
		new File(filename+"/"+id1+"_dic").delete();
		new File(filename+"/"+id1+"_pl").delete();
		new File(filename+"/"+id2+"_dic").delete();
		new File(filename+"/"+id2+"_pl").delete();
		new File(filename+"/"+id1+"_dic_tmp").renameTo(new File(filename+"/"+id1+"_dic"));
		new File(filename+"/"+id1+"_pl_tmp").renameTo(new File(filename+"/"+id1+"_pl"));
		return termNum;
	}

	/**
	 * 当前内存占用情况
	 */
	public void memoryStatus(){
		System.out.println("空闲内存："+Runtime.getRuntime().freeMemory());
		System.out.println("总内存："+Runtime.getRuntime().totalMemory());
		System.out.println("最大内存："+Runtime.getRuntime().maxMemory());
		System.out.println("已占用的内存："+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
		
		//args[0] 为语料的文件夹路径
		//String path = "C:\\Users\\adama\\Desktop\\国科大\\作业\\现代信息检索\\shakespeare-merchant.trec";
		String path = "C:\\Users\\adama\\Desktop\\国科大\\作业\\现代信息检索\\xxx";
		if(args.length > 1) path = args[0];
		TokenStream ts = new TokenStream(path, "content");
		new SPIMI(ts, "WT10G");
		ts = new TokenStream(path, "title");
		new SPIMI(ts, "WT10G\\title");
		System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
	}
}
