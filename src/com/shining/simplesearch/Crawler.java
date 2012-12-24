package com.shining.simplesearch;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

import org.htmlparser.beans.StringBean;

import org.htmlparser.util.ParserException;


import org.mira.lucene.analysis.*;


public class Crawler implements Callable<ArrayList<String>> {

 private HashMap<String, ArrayList<String>> disallowListCache = new HashMap<String, ArrayList<String>>();
 ArrayList<String> errorList = new ArrayList<String>();// 错误信息
 ArrayList<String> result = new ArrayList<String>(); // 搜索到的结果
 String startUrl;// 开始搜索的起点
 int maxUrl;// 最大处理的url数
 static int countUrl=1;

 boolean caseSensitive = false;// 是否区分大小写
 boolean limitHost = false;// 是否在限制的主机内搜索
 
 private String data_path="/data/data";

 public Crawler ( String startUrl, int maxUrl) {
  this.startUrl = startUrl;
  this.maxUrl = maxUrl;
 }
 
 public ArrayList<String> call() throws Exception {
	 
	return crawl(startUrl, maxUrl, limitHost, caseSensitive);
 }

 public ArrayList<String> getResult() {
  
	 return result;
 }


private URL verifyUrl(String url) {
  
  if (!url.toLowerCase().startsWith("http://"))
   
	  return null;
  
  URL verifiedUrl = null;
  try {
   
	  verifiedUrl = new URL(url);
  } catch (Exception e) {
	  
	 return null;
  }
  	return verifiedUrl;
 }


 private boolean isRobotAllowed(URL urlToCheck) {
  String host = urlToCheck.getHost().toLowerCase();

  ArrayList<String> disallowList = disallowListCache.get(host);

  if (disallowList == null) {
   disallowList = new ArrayList<String>();
   try {
    URL robotsFileUrl = new URL("http://" + host + "/robots.txt");
    BufferedReader reader = new BufferedReader(
      new InputStreamReader(robotsFileUrl.openStream()));

    String line;
    while ((line = reader.readLine()) != null) {
     if (line.indexOf("Disallow:") == 0) {
      String disallowPath = line.substring("Disallow:"
        .length());
      
      int commentIndex = disallowPath.indexOf("#");
      if (commentIndex != -1) {
       disallowPath = disallowPath.substring(0,
         commentIndex);
      }

      disallowPath = disallowPath.trim();
      disallowList.add(disallowPath);
     }
    }

    disallowListCache.put(host, disallowList);
   } catch (Exception e) {
    return true; 
   }
  }

  String file = urlToCheck.getFile();
  for (int i = 0; i < disallowList.size(); i++) {
   String disallow = disallowList.get(i);
   if (file.startsWith(disallow)) {
    return false;
   }
  }

  return true;
 }
 

 private String downloadPage(URL pageUrl) {
  try {
	  
	    HttpURLConnection connection = (HttpURLConnection) pageUrl.openConnection();
	    
	    connection.connect();
	       
	    InputStream inputStream = connection.getInputStream();
	        
	    byte bytes[] = new byte[1024*100]; 
	        
	    int index = 0;
	        
	    int count = inputStream.read(bytes, index, 1024*100);
	        
	    while (count != -1) {
	          
	    	index += count;
	          
	    	count = inputStream.read(bytes, index, 1);
	    }

	 BufferedReader reader = new BufferedReader(new InputStreamReader(
     pageUrl.openStream(),"UTF-8"));
   

	 String line;
	 StringBuffer pageBuffer = new StringBuffer();
	 while ((line = reader.readLine()) != null) {
		 pageBuffer.append(line);
	 }
   
	 String buf=pageBuffer.toString();
  
	 OutputStream os;
   
	 try {

	   File file=new File(data_path);
	   if(!file.exists())
		   file.mkdirs();
	   
	
	   
	   os = new FileOutputStream(data_path+"/"+countUrl+".html");
	   os.write(bytes);
	  
	   os.close();
	   } catch (FileNotFoundException e) {
	   e.printStackTrace();
	   } catch (IOException e) {
	   e.printStackTrace();
	   }
	   
	 countUrl++;
   
	 return buf;
  } catch (Exception e) {
	  e.printStackTrace();
  }
  	return null;
 }
 
 private String removeWwwFromUrl(String url) {
  int index = url.indexOf("://www.");
  if (index != -1) {
   return url.substring(0, index + 3) + url.substring(index + 7);
  }

  return (url);
 }


 private ArrayList<String> retrieveLinks(URL pageUrl, String pageContents,
   HashSet crawledList, boolean limitHost) {

  Pattern p = Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]",
    Pattern.CASE_INSENSITIVE);
  Matcher m = p.matcher(pageContents);

  ArrayList<String> linkList = new ArrayList<String>();
  while (m.find()) {
   String link = m.group(1).trim();

   if (link.length() < 1) {
    continue;
   }

   if (link.charAt(0) == '#') {
    continue;
   }

   if (link.indexOf("mailto:") != -1) {
    continue;
   }

   if (link.toLowerCase().indexOf("javascript") != -1) {
    continue;
   }

   if (link.indexOf("://") == -1) {
    if (link.charAt(0) == '/') {
     link = "http://" + pageUrl.getHost() + ":"
       + pageUrl.getPort() + link;
    } else {
     String file = pageUrl.getFile();
     if (file.indexOf('/') == -1) {
      link = "http://" + pageUrl.getHost() + ":"
        + pageUrl.getPort() + "/" + link;
     } else {
      String path = file.substring(0,
        file.lastIndexOf('/') + 1);
      link = "http://" + pageUrl.getHost() + ":"
        + pageUrl.getPort() + path + link;
     }
    }
   }

   int index = link.indexOf('#');
   if (index != -1) {
    link = link.substring(0, index);
   }

   link = removeWwwFromUrl(link);

   URL verifiedLink = verifyUrl(link);
   if (verifiedLink == null) {
    continue;
   }

   if (limitHost
     && !pageUrl.getHost().toLowerCase().equals(
       verifiedLink.getHost().toLowerCase())) {
    continue;
   }

  
   if (crawledList.contains(link)) {
    continue;
   }

   linkList.add(link);
  }

  return (linkList);
 }

 public ArrayList<String> crawl(String startUrl, int maxUrls,
    boolean limithost, boolean caseSensitive) {

  HashSet<String> crawledList = new HashSet<String>();
  LinkedHashSet<String> toCrawlList = new LinkedHashSet<String>();
  ArrayList<String> resultList=new ArrayList<String>();

  if (maxUrls < 1) {
   errorList.add("Invalid Max URLs value.");
   System.out.println("Invalid Max URLs value.");
  }

  if (errorList.size() > 0) {
   System.out.println("err!!!");
   return errorList;
  }

  startUrl = removeWwwFromUrl(startUrl);

  toCrawlList.add(startUrl);
  while (toCrawlList.size() > 0) {

   if (maxUrls != -1) {
    if (crawledList.size() == maxUrls) {
     break;
    }
   }


   String url = toCrawlList.iterator().next();


   toCrawlList.remove(url);

  
   URL verifiedUrl = verifyUrl(url);


   if (!isRobotAllowed(verifiedUrl)) {
    continue;
   }

   crawledList.add(url);
   resultList.add(url);
   String pageContents = downloadPage(verifiedUrl);
   
   
  

   if (pageContents != null && pageContents.length() > 0) {
  
	   ArrayList<String> links = retrieveLinks(verifiedUrl,
      pageContents, crawledList, limitHost);

    toCrawlList.addAll(links);
    }

  }
  
	new Thread(new Index(data_path)).start();
  
	return resultList;
 }
}


