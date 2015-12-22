package index.util;

import java.io.File;

public class FileOperation {
	
	/**
	 * 创建文件夹
	 */
	public static void createFolder(File file){
		if(file.exists() && file.isDirectory()) deleteFile(file);
		file.mkdirs();
	}
	
	/**
	 * 删除文档或文件
	 */
	public static void deleteFile(File file){
		if(file.isDirectory()){
			File[] files = file.listFiles();
			for(int i=0; i<files.length; i++){
				deleteFile(files[i]);
			}
		}
		file.delete();
	}
}