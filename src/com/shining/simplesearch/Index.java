package com.shining.simplesearch;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.text.Highlighter;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.mira.lucene.analysis.IK_CAnalyzer;

import org.htmlparser.*;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;


public class Index implements Runnable{
	

	
	private String index_path="/data/index";
	
	private String file_path;

	public Index(String file_path){
		
		this.file_path=file_path;
	}
	
	public void run(){
		
	
		
		try {
			
			index(file_path);
		}
		catch (ParserException e) {
			
			e.printStackTrace();
			
		}
		 catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
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
		 
	
	
	public void index(String filesPath) throws IOException, ParserException  
	 {  
		  File dataDir=new File(filesPath);		
	      IK_CAnalyzer ik=new IK_CAnalyzer();
          IndexWriter indexWriter = new IndexWriter(index_path,ik,true);   
          
         
          
          Document doc;

          for(File file:dataDir.listFiles()){
        	  
	
	         doc = new Document();  
	         
	         String filename=file.getName();
	         doc.add(new Field("filename",filename,Field.Store.YES,Field.Index.UN_TOKENIZED));
	         
	         String uri=file.getPath();
	         doc.add(new Field("uri",uri,Field.Store.YES,Field.Index.NO));
	
	         
	         Date dt=new Date(file.lastModified());
	         SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd E");
	         String cdate=sdf.format(dt);
	         doc.add(new Field("cdate",cdate,Field.Store.YES,Field.Index.NO));
	         
	         double si=file.length();
	         String size="";
	         
	         if(si>1024){
	        	 size=String.valueOf(Math.floor(si/1024))+"K";
	         }
	         else{
	        	 size=String.valueOf(si)+"Bytes";
	        	 
	         }
	         
	         
	         doc.add(new Field("size",size,Field.Store.YES,Field.Index.NO));
	         
	         String text=getText(uri);
	         doc.add(new Field("text",text,Field.Store.COMPRESS,Field.Index.TOKENIZED,Field.TermVector.WITH_POSITIONS_OFFSETS));
	         
	         String digest="";
	         if(text.length()>200){
	        	 digest=text.substring(0,200);
	         }
	         else{
	        	 digest=text;
	         }
	         
	         doc.add(new Field("digest",digest,Field.Store.YES,Field.Index.UN_TOKENIZED));
	         
	 
	     
	         indexWriter.addDocument(doc);  
	         
          }
	   

	       
	        
	       indexWriter.optimize();  
	       indexWriter.close();  
	     
	 }


}
