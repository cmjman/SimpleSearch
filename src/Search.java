import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.mira.lucene.analysis.IK_CAnalyzer;


public class Search implements Callable<Hits>{

	private IndexSearcher searcher;
	
	private Query query;
	
	private String index_path="/data/index";
	private String keyword;
	
	 IK_CAnalyzer ik_analyzer=new IK_CAnalyzer();
	 
	
	
	public Search(String keyword ){
		
		this.keyword=keyword;
		
		 try  
		    {  
		     
		     searcher = new IndexSearcher(IndexReader.open(index_path));  
		    }catch(Exception e)  
		    {  
		      e.printStackTrace();  
		    }  
	}
	
	 public Hits call() throws Exception {
		 
		 Date start = new Date();  
		    //对我们索引的content字段进行搜索  
		    QueryParser qp = new QueryParser("content",ik_analyzer);  
		    this.query = qp.parse(keyword);  
		   
		    Hits hits = this.searcher.search(query);  
		    
		    Date end = new Date();  
		    System.out.println("检索完成,用时"+(end.getTime()-start.getTime())+"毫秒");  
		    //////////打印测试////////  
		   
		    if(hits != null && hits.length() > 0)  
		    {   
		    	
		      for(int i = 0; i < hits.length(); i++)  
		      {  
		        try  
		        {  
		          Document doc = hits.doc(i);  
		          System.out.println("结果"+(i+1)+":"+doc.get("title")+" createTime:"+doc.get("content"));   
		          //System.out.println(doc.get("path"));  
		        }catch(Exception e)  
		        {  
		          e.printStackTrace();  
		        }  
		      }  
		    }  
		    return hits;  
		 
	 }
	 
	 
}
