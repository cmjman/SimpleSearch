package com.shining.simplesearch;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;



public class UserInterface extends JFrame{

	private static final long serialVersionUID = 1L;
	private JButton button_crawl=new JButton("抓取");
	private JButton button_search=new JButton("搜索");
	private JButton button_clear=new JButton("清空");
	private JLabel label_crawl=new JLabel("请在此输入网址：");
	private JLabel label_search=new JLabel("请在此输入需要搜索的关键词：");
	private JTextField text_url=new JTextField("www.hdu.edu.cn",10);
	private JTextField text_keyword=new JTextField("杭州",10);
	private JTextArea text_output_west=new JTextArea(30,20);

	
	private JPanel panel_north=new JPanel();
	private JPanel panel_west=new JPanel();
	private JPanel panel_center=new JPanel();
	private JPanel panel_south=new JPanel();
	
	private JEditorPane editorPane = new JEditorPane();
	

	private String url_input;
	private Callable<ArrayList<String>> crawl;
	private Callable<String> search;
	
	public static ArrayList<String> list;
	
	private String keyword;
	
	
	public  UserInterface() throws IOException{
		
		super("SearchEngine");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.setResizable(false);
		
		text_output_west.setEditable(false);
		editorPane.setEditable(false); 
		editorPane.setContentType("text/html");
		editorPane.setPreferredSize(new Dimension(480,480));
	
		panel_north.setLayout(new FlowLayout());
		panel_north.add(label_crawl);
		panel_north.add(text_url);
		panel_north.add(button_crawl);
		
		panel_west.setLayout(new FlowLayout());
		panel_west.add(new JScrollPane(text_output_west));
		
		panel_center.setLayout(new FlowLayout());
		panel_center.add(new JScrollPane(editorPane));
		
		panel_south.setLayout(new FlowLayout());
		panel_south.add(label_search);
		panel_south.add(text_keyword);
		panel_south.add(button_search);
		panel_south.add(button_clear);
		
		this.add(panel_north,BorderLayout.NORTH);
		this.add(panel_west,BorderLayout.WEST);
		this.add(panel_center,BorderLayout.CENTER);
		this.add(panel_south,BorderLayout.SOUTH);
		this.pack();
		this.setVisible(true);
		
	
		editorPane.addHyperlinkListener(new HyperlinkListener(){
			
			public void hyperlinkUpdate(HyperlinkEvent e){
				
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)  
			      {  
			         JEditorPane pane = (JEditorPane) e.getSource();  
			         if (e instanceof HTMLFrameHyperlinkEvent)  
			         {  
			            HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;  
			            HTMLDocument doc = (HTMLDocument) pane.getDocument();  
			            doc.processHTMLFrameHyperlinkEvent(evt);  
			         }  
			         else  
			         {  
			            try  
			            {  
			               pane.setPage(e.getURL());  
			            }  
			            catch (Throwable t)  
			            {  
			               t.printStackTrace();  
			            }  
			         }  
			      }  
			}
		});
		
		
		button_crawl.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				
				text_output_west.append("开始抓取，请稍等...\n");
				
				url_input=text_url.getText();
				
				if(!url_input.contains("http"))
					url_input="http://"+url_input+"/";
				
			
				ExecutorService threadPool = Executors.newSingleThreadExecutor(); 
				
				crawl = new Crawler(url_input, 20);
				 
				Future<ArrayList<String>> future =(Future<ArrayList<String>>) threadPool.submit(crawl);
				
				threadPool.shutdown();
				
				
				list=new ArrayList<String>();
			
				try {
					for(String s:future.get()){
						
						list.add(s);
						
						text_output_west.append(s+"\n");
						 
					 }
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					e1.printStackTrace();
				}
				
				text_output_west.append("抓取并索引完成！");
			}
		});
		
		button_search.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				
				ExecutorService threadPool = Executors.newSingleThreadExecutor(); 
				
				keyword=text_keyword.getText();
				search =new Search(keyword);
				
				Future<String> future =(Future<String>) threadPool.submit(search);
				 
				 threadPool.shutdown();
				 
				 String searchResult;
				try {
					searchResult = future.get();
					
					editorPane.setText(searchResult);
					
				} catch (InterruptedException e1) {
				
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					
					e1.printStackTrace();
				}
				 
				
			}
		});
		
		button_clear.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				
				editorPane.setText("");
				editorPane.setBackground(Color.WHITE);
			
			}
		});
	}
	
	public static void main(String[] args) throws IOException{
		new UserInterface();
	}
}
