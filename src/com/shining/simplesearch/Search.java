package com.shining.simplesearch;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenSources;
import org.mira.lucene.analysis.IK_CAnalyzer;


public class Search implements Callable<String>{

	private IndexSearcher searcher;
	
	private Query query;
	
	private String index_path="/data/index";
	private String keyword;
	private String result;
	
	IK_CAnalyzer ik_analyzer=new IK_CAnalyzer();
	 
	public Search(String keyword ){
		
		this.keyword=keyword;
		
		 try{  
		     
			 searcher = new IndexSearcher(IndexReader.open(index_path));  
			 
		 }catch(Exception e){  
		      
			 e.printStackTrace();  
		    
		 }  
	}
	
	 public String call() throws Exception {
		 
		 Date start = new Date();  

		    QueryParser qp = new QueryParser("text",ik_analyzer);  
		    this.query = qp.parse(keyword);  
		   
		    Hits hits = this.searcher.search(query);  
		    
		    SimpleHTMLFormatter shf=new SimpleHTMLFormatter("<span style='background:red'>","</span>");
		    Highlighter highlighter=new Highlighter(shf,new QueryScorer(query));
		    
		    SimpleFragmenter sf=new SimpleFragmenter(100);
		    highlighter.setTextFragmenter(sf);
		    
			
		
		    
		    Date end = new Date();  
		    String time="检索完成,用时"+(end.getTime()-start.getTime())+"毫秒<br>检索结果如下：<p>";  
		    
		    result=time;
	
		    if(hits != null && hits.length() > 0)  
		    {   
		    
		    	
		      for(int i = 0; i < hits.length(); i++)  
		      {  
		        try  
		        {  
		          Document doc = hits.doc(i);  
		       //   System.out.println("结果"+(i+1)+":"+doc.get("uri")+" createTime:"+doc.get("text"));
		          
		          String filename=doc.getField("filename").stringValue();
		          String uri=doc.getField("uri").stringValue();
		  //        String digest=doc.getField("digest").stringValue();
		          String text=doc.getField("text").stringValue();
					 
					 
					 
			TermPositionVector tpv=(TermPositionVector)searcher.getIndexReader().getTermFreqVector(hits.id(i), "text");
			TokenStream tokenStream=TokenSources.getTokenStream(tpv);
			result=result+"Uri:C:\\"+uri+"<br>Filename:"+filename+"<br>Text:"+
					highlighter.getBestFragment(tokenStream, text)+"<br><a href='"+
						UserInterface.list.get(Integer.parseInt(filename.replace(".html", "")))+"'>原网页</a><p>";
			
	//		System.out.println(filename.replace(".html", ""));
		          
			
		          
		        }catch(Exception e)  
		        {  
		          e.printStackTrace();  
		        }  
		      }  
		    }  
		    return result;  
		 
	 }
	 
	 
}
