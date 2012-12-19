import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import org.htmlparser.beans.StringBean;

import org.htmlparser.util.ParserException;


import org.mira.lucene.analysis.*;




public class SearchEngine implements Runnable,Callable<ArrayList<String>> {

 private HashMap<String, ArrayList<String>> disallowListCache = new HashMap<String, ArrayList<String>>();
 ArrayList<String> errorList = new ArrayList<String>();// 错误信息
 ArrayList<String> result = new ArrayList<String>(); // 搜索到的结果
 String startUrl;// 开始搜索的起点
 int maxUrl;// 最大处理的url数
 static int countUrl=0;
// String searchString;// 要搜索的字符串(英文)
 boolean caseSensitive = false;// 是否区分大小写
 boolean limitHost = false;// 是否在限制的主机内搜索
 
 

 public SearchEngine ( String startUrl, int maxUrl) {
  this.startUrl = startUrl;
  this.maxUrl = maxUrl;
  //this.searchString = searchString;
 }
 
 public ArrayList<String> call() throws Exception {
	 
	 ArrayList<String> temp= crawl(startUrl, maxUrl, limitHost, caseSensitive);
	 
	 System.out.println(temp);
	return temp;
 }

 public ArrayList<String> getResult() {
  return result;
 }

 public void run() {// 启动搜索线程
  crawl(startUrl, maxUrl, limitHost, caseSensitive);
  
  
 
 }

 // 检测URL格式
 private URL verifyUrl(String url) {
  // 只处理HTTP URLs.
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

 // 检测robot是否允许访问给出的URL.
 private boolean isRobotAllowed(URL urlToCheck) {
  String host = urlToCheck.getHost().toLowerCase();// 获取给出URL的主机

  // 获取主机不允许搜索的URL缓存
  ArrayList<String> disallowList = disallowListCache.get(host);

  // 如果还没有缓存,下载并缓存。
  if (disallowList == null) {
   disallowList = new ArrayList<String>();
   try {
    URL robotsFileUrl = new URL("http://" + host + "/robots.txt");
    BufferedReader reader = new BufferedReader(
      new InputStreamReader(robotsFileUrl.openStream()));

    // 读robot文件，创建不允许访问的路径列表。
    String line;
    while ((line = reader.readLine()) != null) {
     if (line.indexOf("Disallow:") == 0) {// 是否包含"Disallow:"
      String disallowPath = line.substring("Disallow:"
        .length());// 获取不允许访问路径

      // 检查是否有注释。
      int commentIndex = disallowPath.indexOf("#");
      if (commentIndex != -1) {
       disallowPath = disallowPath.substring(0,
         commentIndex);// 去掉注释
      }

      disallowPath = disallowPath.trim();
      disallowList.add(disallowPath);
     }
    }

    // 缓存此主机不允许访问的路径。
    disallowListCache.put(host, disallowList);
   } catch (Exception e) {
    return true; // web站点根目录下没有robots.txt文件,返回真
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
 
 
 public String segmentation(String text) throws IOException {
   
     String segText=null;
     
	 StringReader sr=new StringReader(text);
	
	 //调用 IK Analyzer进行分词
	 IK_CAnalyzer i=new IK_CAnalyzer();
	 
	 TokenStream ts=i.tokenStream(" ",sr);
	 
	 Token t=null;
	 while((t=ts.next())!=null)
	 {
		 segText=segText+t.termText()+" ";
		
	 }
	 //返回解析后的分词信息
     return segText;
  }
 
 

 private String downloadPage(URL pageUrl) {
  try {
   // Open connection to URL for reading.
   BufferedReader reader = new BufferedReader(new InputStreamReader(
     pageUrl.openStream(),"UTF-8"));
   
   // Read page into buffer.
   String line;
   StringBuffer pageBuffer = new StringBuffer();
   while ((line = reader.readLine()) != null) {
    pageBuffer.append(line);
  
   }
   
   String buf=pageBuffer.toString();
   

//	byte[] bytes=segmentation(getText(pageUrl.toString())).getBytes();
   byte[] bytes=getText(pageUrl.toString()).getBytes();
   
 //  File file=new File("web"+countUrl+".txt");
   OutputStream os;
   
   try {
	   String save_path="SearchResult";
	   File file=new File(save_path);
	   if(!file.exists())
		   file.mkdirs();
	   
	   os = new FileOutputStream(save_path+"/web"+countUrl+".txt");
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
  }
  return null;
 }
 





// 从URL中去掉"www"
 private String removeWwwFromUrl(String url) {
  int index = url.indexOf("://www.");
  if (index != -1) {
   return url.substring(0, index + 3) + url.substring(index + 7);
  }

  return (url);
 }

 // 解析页面并找出链接
 private ArrayList<String> retrieveLinks(URL pageUrl, String pageContents,
   HashSet crawledList, boolean limitHost) {
  // 用正则表达式编译链接的匹配模式。
  Pattern p = Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]",
    Pattern.CASE_INSENSITIVE);
  Matcher m = p.matcher(pageContents);

  ArrayList<String> linkList = new ArrayList<String>();
  while (m.find()) {
   String link = m.group(1).trim();

   if (link.length() < 1) {
    continue;
   }

   // 跳过链到本页面内链接。
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
    if (link.charAt(0) == '/') {// 处理绝对地
     link = "http://" + pageUrl.getHost() + ":"
       + pageUrl.getPort() + link;
    } else {
     String file = pageUrl.getFile();
     if (file.indexOf('/') == -1) {// 处理相对地址
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

   /* 如果限定主机，排除那些不合条件的URL */
   if (limitHost
     && !pageUrl.getHost().toLowerCase().equals(
       verifiedLink.getHost().toLowerCase())) {
    continue;
   }

   // 跳过那些已经处理的链接.
   if (crawledList.contains(link)) {
    continue;
   }

   linkList.add(link);
  }

  return (linkList);
 }

 // 搜索下载Web页面的内容，判断在该页面内有没有指定的搜索字符串

 private boolean searchStringMatches(String pageContents,
   String searchString, boolean caseSensitive) {
  String searchContents = pageContents;
  if (!caseSensitive) {// 如果不区分大小写
   searchContents = pageContents.toLowerCase();
  }

  Pattern p = Pattern.compile("[\\s]+");
  String[] terms = p.split(searchString);
  for (int i = 0; i < terms.length; i++) {
   if (caseSensitive) {
    if (searchContents.indexOf(terms[i]) == -1) {
     return false;
    }
   } else {
    if (searchContents.indexOf(terms[i].toLowerCase()) == -1) {
     return false;
    }
   }
  }

  return true;
 }

 // 执行实际的搜索操作
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

  // 从开始URL中移出www
  startUrl = removeWwwFromUrl(startUrl);

  toCrawlList.add(startUrl);
  while (toCrawlList.size() > 0) {

   if (maxUrls != -1) {
    if (crawledList.size() == maxUrls) {
     break;
    }
   }

   // Get URL at bottom of the list.
   String url = toCrawlList.iterator().next();

   // Remove URL from the to crawl list.
   toCrawlList.remove(url);

   // Convert string url to URL object.
   URL verifiedUrl = verifyUrl(url);

   // Skip URL if robots are not allowed to access it.
   if (!isRobotAllowed(verifiedUrl)) {
    continue;
   }

   // 增加已处理的URL到crawledList
   crawledList.add(url);
   resultList.add(url);
   String pageContents = downloadPage(verifiedUrl);
   
   
  

   if (pageContents != null && pageContents.length() > 0) {
    // 从页面中获取有效的链接
	   ArrayList<String> links = retrieveLinks(verifiedUrl,
      pageContents, crawledList, limitHost);

    toCrawlList.addAll(links);
    

   
    
   }
 //  System.out.println(url);
  }
  
  
  return resultList;
 }
 
/*


 // 主函数
 public static void main(String[] args) { 
	 
	 new Thread(new Runnable() { 
		 public void run() { 
		 while(true) { 
			 
			 
			 Scanner in =new Scanner(System.in);
			 System.out.println("Please input the url you want to search...");
			 String inputURL=in.next();

			 if(!inputURL.contains("http")){
				 inputURL="http://"+inputURL+"/";
			 }
			  
			 System.out.println("Please input the word you want to search...");
			 String inputWORD=in.next();
			  
			Spider crawler =new Spider("http://www.hdu.edu.cn/",20,"杭州");
			 SearchEngine crawler = new SearchEngine(inputURL, 20,inputWORD);
			 Thread search = new Thread(crawler);
			  
			 System.out.println("Start searching...");
			 System.out.println("result:");
			 search.start();
			 
			 
			
			 try {
				 
			  search.join();
			  
		
			  
			  Thread.sleep(3000);
			  
			
			  
			 } catch (InterruptedException e) {
			  e.printStackTrace();
			 }catch (Exception e){
			 	e.printStackTrace();
			 }
		 	}
		 } 
		 }).start(); 
 	}
 	*/
}


