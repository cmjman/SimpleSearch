import java.io.BufferedReader;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import org.htmlparser.beans.StringBean;

import org.htmlparser.util.ParserException;


import org.mira.lucene.analysis.*;




public class Spider implements Runnable {

 private HashMap<String, ArrayList<String>> disallowListCache = new HashMap<String, ArrayList<String>>();
 ArrayList<String> errorList = new ArrayList<String>();// ������Ϣ
 ArrayList<String> result = new ArrayList<String>(); // �������Ľ��
 String startUrl;// ��ʼ���������
 int maxUrl;// ������url��
 static int countUrl=0;
 String searchString;// Ҫ�������ַ���(Ӣ��)
 boolean caseSensitive = false;// �Ƿ����ִ�Сд
 boolean limitHost = false;// �Ƿ������Ƶ�����������

 public Spider ( String startUrl, int maxUrl, String searchString) {
  this.startUrl = startUrl;
  this.maxUrl = maxUrl;
  this.searchString = searchString;
 }

 public ArrayList<String> getResult() {
  return result;
 }

 public void run() {// ���������߳�
  crawl(startUrl, maxUrl, searchString, limitHost, caseSensitive);
 }

 // ���URL��ʽ
 private URL verifyUrl(String url) {
  // ֻ����HTTP URLs.
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

 // ���robot�Ƿ�������ʸ�����URL.
 private boolean isRobotAllowed(URL urlToCheck) {
  String host = urlToCheck.getHost().toLowerCase();// ��ȡ����URL������

  // ��ȡ����������������URL����
  ArrayList<String> disallowList = disallowListCache.get(host);

  // �����û�л���,���ز����档
  if (disallowList == null) {
   disallowList = new ArrayList<String>();
   try {
    URL robotsFileUrl = new URL("http://" + host + "/robots.txt");
    BufferedReader reader = new BufferedReader(
      new InputStreamReader(robotsFileUrl.openStream()));

    // ��robot�ļ���������������ʵ�·���б�
    String line;
    while ((line = reader.readLine()) != null) {
     if (line.indexOf("Disallow:") == 0) {// �Ƿ����"Disallow:"
      String disallowPath = line.substring("Disallow:"
        .length());// ��ȡ���������·��

      // ����Ƿ���ע�͡�
      int commentIndex = disallowPath.indexOf("#");
      if (commentIndex != -1) {
       disallowPath = disallowPath.substring(0,
         commentIndex);// ȥ��ע��
      }

      disallowPath = disallowPath.trim();
      disallowList.add(disallowPath);
     }
    }

    // �����������������ʵ�·����
    disallowListCache.put(host, disallowList);
   } catch (Exception e) {
    return true; // webվ���Ŀ¼��û��robots.txt�ļ�,������
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
       
     //���ò���Ҫ�õ�ҳ����������������Ϣ  
     sb.setLinks(false);  
     //���ý�����Ͽո�������ո������  
     sb.setReplaceNonBreakingSpaces(true);  
     //���ý�һ���пո���һ����һ�ո�������  
     sb.setCollapse(true);  
     //����Ҫ������URL  
     sb.setURL(url);  
     //���ؽ��������ҳ���ı���Ϣ  
     return sb.getStrings();  
 }  
 
 
 public String segmentation(String text) throws IOException {
   
     String segText=null;
     
	 StringReader sr=new StringReader(text);
	
	 //���� IK Analyzer���зִ�
	 IK_CAnalyzer i=new IK_CAnalyzer();
	 
	 TokenStream ts=i.tokenStream(" ",sr);
	 
	 Token t=null;
	 while((t=ts.next())!=null)
	 {
		 segText=segText+t.termText()+" ";
		
	 }
	 //���ؽ�����ķִ���Ϣ
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
   

	byte[] bytes=segmentation(getText(pageUrl.toString())).getBytes();
   
 //  File file=new File("web"+countUrl+".txt");
   OutputStream os;
   
   try {
	   os = new FileOutputStream("web"+countUrl+".txt");
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
 





// ��URL��ȥ��"www"
 private String removeWwwFromUrl(String url) {
  int index = url.indexOf("://www.");
  if (index != -1) {
   return url.substring(0, index + 3) + url.substring(index + 7);
  }

  return (url);
 }

 // ����ҳ�沢�ҳ�����
 private ArrayList<String> retrieveLinks(URL pageUrl, String pageContents,
   HashSet crawledList, boolean limitHost) {
  // ��������ʽ�������ӵ�ƥ��ģʽ��
  Pattern p = Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]",
    Pattern.CASE_INSENSITIVE);
  Matcher m = p.matcher(pageContents);

  ArrayList<String> linkList = new ArrayList<String>();
  while (m.find()) {
   String link = m.group(1).trim();

   if (link.length() < 1) {
    continue;
   }

   // ����������ҳ�������ӡ�
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
    if (link.charAt(0) == '/') {// ������Ե�
     link = "http://" + pageUrl.getHost() + ":"
       + pageUrl.getPort() + link;
    } else {
     String file = pageUrl.getFile();
     if (file.indexOf('/') == -1) {// ������Ե�ַ
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

   /* ����޶��������ų���Щ����������URL */
   if (limitHost
     && !pageUrl.getHost().toLowerCase().equals(
       verifiedLink.getHost().toLowerCase())) {
    continue;
   }

   // ������Щ�Ѿ����������.
   if (crawledList.contains(link)) {
    continue;
   }

   linkList.add(link);
  }

  return (linkList);
 }

 // ��������Webҳ������ݣ��ж��ڸ�ҳ������û��ָ���������ַ���

 private boolean searchStringMatches(String pageContents,
   String searchString, boolean caseSensitive) {
  String searchContents = pageContents;
  if (!caseSensitive) {// ��������ִ�Сд
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

 // ִ��ʵ�ʵ���������
 public ArrayList<String> crawl(String startUrl, int maxUrls,
   String searchString, boolean limithost, boolean caseSensitive) {

  HashSet<String> crawledList = new HashSet<String>();
  LinkedHashSet<String> toCrawlList = new LinkedHashSet<String>();

  if (maxUrls < 1) {
   errorList.add("Invalid Max URLs value.");
   System.out.println("Invalid Max URLs value.");
  }

  if (searchString.length() < 1) {
   errorList.add("Missing Search String.");
   System.out.println("Missing search String");
  }

  if (errorList.size() > 0) {
   System.out.println("err!!!");
   return errorList;
  }

  // �ӿ�ʼURL���Ƴ�www
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

   // �����Ѵ����URL��crawledList
   crawledList.add(url);
   String pageContents = downloadPage(verifiedUrl);
   
   
  

   if (pageContents != null && pageContents.length() > 0) {
    // ��ҳ���л�ȡ��Ч������
    ArrayList<String> links = retrieveLinks(verifiedUrl,
      pageContents, crawledList, limitHost);

    toCrawlList.addAll(links);

    if (searchStringMatches(pageContents, searchString,
      caseSensitive)) {
     result.add(url);
     System.out.println(url);
    }
   }

  }
  return result;
 }
 



 // ������
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
			  
			//Spider crawler =new Spider("http://www.hdu.edu.cn/",20,"����");
			 Spider crawler = new Spider(inputURL, 20,inputWORD);
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
}


