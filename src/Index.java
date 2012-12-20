import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.mira.lucene.analysis.IK_CAnalyzer;

import org.htmlparser.*;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;


public class Index implements Runnable{
	
	private String data_path="/data/data";
	
	private String index_path="/data/index";
	
	private String file_path;

	public Index(String file_path){
		
		this.file_path=file_path;
	}
	
	public void run(){
		
		
		
	}
	
	
	 public String getText(String url)throws ParserException{  
	     StringBean sb = new StringBean();  
	       
	     //设置不需要得到页面所包含的链接信息  
	     sb.setLinks(false);  
	     //设置将不间断空格由正规空格所替代  
	     sb.setReplaceNonBreakingSpaces(true);  
	     //设置将一序列空格由一个单一空格所代替  
	     sb.setCollapse(true);  
	     //传入要解析的URL  
	     sb.setURL(url);  
	     //返回解析后的网页纯文本信息  
	     return sb.getStrings();  
	 }  
		 
	
	
	public void index(String filesPath) throws IOException  
	 {  
		  File dataDir=new File(filesPath);		
	      IK_CAnalyzer ik=new IK_CAnalyzer();
          IndexWriter indexWriter = new IndexWriter(index_path,ik,true);   
          
          Document doc;

          for(File file:dataDir.listFiles()){
        	  
        	  int i=Integer.parseInt(file.getName());
	
	         doc = new Document();  
	         doc.add(new Field("url",Crawl.UrlList.get(i),Field.Store.YES,Field.Index.TOKENIZED));  
	     
	         indexWriter.addDocument(doc);  
	         
          }
	   
	       
	        
	       indexWriter.optimize();  
	       indexWriter.close();  
	     
	 }


}
