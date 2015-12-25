package setup;

import index.domain.Dictionary;
import index.domain.Document;

import java.io.File;
import java.io.IOException;

import retrieve.util.DocInfo;

public class Initializer {
	public static String projectPath = "i:\\kuaipan\\graduateCourses\\IR\\program\\IR_system\\";
	public static String dataPath = projectPath + "data\\";
	public static String indexPath = dataPath + "index\\";
	public static String docPath = dataPath + "WT10G\\WT10G_toy\\";
	
	public static void main(String[] args) throws NumberFormatException, IOException{
		//writeVectorLength();
		String truthFileName= dataPath+"/WT10G/qrels.trec9_10";
		String outputFeaturesFileName = projectPath + "RankLib\\features_all.txt";
		String queryFileName = dataPath + "\\WT10G\\querys.txt";
		TrainLambdaMART.genTrainingData(indexPath, truthFileName, queryFileName, outputFeaturesFileName);
	}
	public static void writeVectorLength() throws IOException{
		String[] docNoByDocId = Document.loadDocNoByDocId(new File(indexPath + "/docNoByDocId"));
		int N = docNoByDocId.length;
		System.out.println(N+"");
		Dictionary contentDic = Dictionary.load(
					new File(indexPath + "/dictionary"), 
					new File(indexPath + "/postinglist"));
		System.out.println("content dictionary loaded");
		DocInfo.computeWriteDocTfIdfVectorLengths(contentDic, docNoByDocId, N, docPath, indexPath+"/docWeightLength.txt");
	}
}
