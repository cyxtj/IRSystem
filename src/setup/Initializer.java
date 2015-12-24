package setup;

import index.domain.Dictionary;
import index.domain.Document;

import java.io.File;
import java.io.IOException;

import retrieve.util.DocInfo;

public class Initializer {
	public static String indexPath = "i:\\kuaipan\\graduateCourses\\IR\\program\\IR_system\\data\\index\\";
	public static String docPath = "i:\\kuaipan\\graduateCourses\\IR\\program\\IR_system\\data\\WT10G\\WT10G_copy\\";
	public static void main(String[] args) throws NumberFormatException, IOException{
		writeVectorLength();
		TrainLambdaMART.genTrainingData();
	}
	public static void writeVectorLength() throws IOException{
		String[] docNoByDocId = Document.loadDocNoByDocId(new File(indexPath + "/docNoByDocId"));
		int N = docNoByDocId.length;
		System.out.println(N+"");
		Dictionary contentDic = Dictionary.load(
					new File(indexPath + "/dictionary"), 
					new File(indexPath + "/postinglist"));
		System.out.println("loaded");
		DocInfo.computeWriteDocTfIdfVectorLengths(contentDic, docNoByDocId, N, docPath, indexPath+"/docWeightLength.txt");
	}
}
