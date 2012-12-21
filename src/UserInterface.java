import java.awt.BorderLayout;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;



public class UserInterface extends JFrame{

	private JButton button_crawl=new JButton("抓取");
	private JButton button_search=new JButton("搜索");
	private JLabel label_crawl=new JLabel("请在此输入网址：");
	private JLabel label_search=new JLabel("请在此输入需要搜索的关键词：");
	private JTextField text_url=new JTextField("www.hdu.edu.cn",10);
	private JTextField text_keyword=new JTextField("杭州",10);
	private JTextArea text_output=new JTextArea(20,60);
	
	private JPanel p1=new JPanel();
	private JPanel p2=new JPanel();
	private JPanel p3=new JPanel();

	private String url_input;
	private Callable<ArrayList<String>> crawl;
	private Callable<Hits> search;
	
	private ArrayList<String> craw_result;
	
	
	public  UserInterface(){
		
		super("SearchEngine");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.setSize(320, 240);
		
	//	this.setBounds(320, 320, 320, 320);
		this.setResizable(false);
		
		
		
		
		button_crawl.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				url_input=text_url.getText();
				
				if(!url_input.contains("http"))
					url_input="http://"+url_input+"/";
				
				 text_output.append("开始抓取，请稍等...\n");
				 
				  ExecutorService threadPool = Executors.newSingleThreadExecutor(); 
					
				 crawl = new Crawler(url_input, 20);
				 
			//	 thread_search = nexw Callable(search);
				 
				 
				 Future<ArrayList<String>> future =(Future<ArrayList<String>>) threadPool.submit(crawl);
				 
				 threadPool.shutdown();
				 
				
				 
			//	search.run();
				 
			//	 thread_search.start();
				
				/*
				 
				try {
					thread_search.join();
				} catch (InterruptedException e1) {
					
					e1.printStackTrace();
				}
				*/
				
				
				
				
			//	 craw_result=search.getResult();
			//	 System.out.println(craw_result);
				 text_output.append("抓取到的网址如下...\n");
				 
				 try {
						
					for(String s:future.get()){
						
					
						
						//	System.out.println(s);

							text_output.append(s+"\n");
						 
					 }
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		button_search.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				
				ExecutorService threadPool = Executors.newSingleThreadExecutor(); 
				
				search =new Search(text_keyword.getText());
				
				Future<Hits> future =(Future<Hits>) threadPool.submit(search);
				 
				 threadPool.shutdown();
				 
				 Hits h;
				try {
					h = future.get();
				
				 
				 int num=h.length();
				 
				 
				 for(int i=0;i<num;i++){
						
					
					try {
						 Document doc=null;
						
						 doc = h.doc(i);
					
					 if(doc == null )
						 continue;
						
					 String filename=doc.getField("filename").stringValue();
					 String uri=doc.getField("uri").stringValue();
					 String digest=doc.getField("digest").stringValue();
					 String text=doc.getField("text").stringValue();
					
					
					 	text_output.append("uri"+uri+"\n");
						text_output.append("filename:"+filename+"\n");
						text_output.append("digest:"+digest+"\n");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				 }
				 
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (ExecutionException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
			}
		});
		
		text_output.setEditable(false);
		
		
		p1.setLayout(new FlowLayout());
		p1.add(label_crawl);
		p1.add(text_url);
		p1.add(button_crawl);
		
		p2.setLayout(new FlowLayout());
		p2.add(new JScrollPane(text_output));
		
		
		p3.setLayout(new FlowLayout());
		p3.add(label_search);
		p3.add(text_keyword);
		p3.add(button_search);
		
		this.add(p1,BorderLayout.NORTH);
		this.add(p2,BorderLayout.CENTER);
		this.add(p3,BorderLayout.SOUTH);
		this.pack();
		this.setVisible(true);
	}
	
	public static void main(String[] args){
		new UserInterface();
	}
	
}
