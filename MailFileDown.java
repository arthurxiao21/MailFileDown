import java.io.*;
import java.util.*;

import java.util.regex.*;

import javax.mail.*;
import javax.mail.util.*;
import javax.mail.search.*;
import javax.mail.event.*;
import javax.mail.internet.*;
import javax.activation.*;
/**
 * 程序运行后首先输入周数
 * 对于不同周会生成形如"week1.dat"的记录文件和"week1作业目录"
 * week1.dat中保存学号和学生姓名
 * 如下
 * 15031715 XXX
 * 15031720 XX
 * 需要填写邮件服务器地址，账号，密码,不同的协议和邮箱服务提供商，邮件服务器地址不同
 * QQ邮箱使用特殊的安全机制，需要在邮箱设置中开启对应pop3或imap服务，并获取登录密码
 * 使用正则进行对邮件主题的匹配
 * 匹配样式  “java高阶第(1)周作业+(15031715)+(肖剑)”
 * 匹配成功且不在已提交列表week1.dat中，则进行下载
 * @author Arthur
 *
 */
public class MailFileDown {
	/**
	 * 	向记录文件中写入记录
	 * @param week
	 * @param id
	 * @param name
	 * @throws IOException
	 */

	public static void add(int week,String id, String name)throws IOException{
		File file = new File("week"+week+".dat");
		synchronized(file){
			FileWriter fw = new FileWriter("week"+week+".dat",true);
			fw.write(id+" "+name+"\n");
			fw.close();
		}
	}
	
	/**
	 * 	查找记录文件中条目
	 * @param week
	 * @param id
	 * @return 记录是否存在
	 */
	public static boolean find(int week,String id){
		File file = new File("week"+week+".dat");
		
		//使用List 存储每行字符
		List<String> content = new ArrayList<String>();  
	    try {  
	        FileInputStream fileInputStream = new FileInputStream(file);  
	        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);  
	        BufferedReader reader = new BufferedReader(inputStreamReader);  
	        String lineContent = "";  
	        while ((lineContent = reader.readLine()) != null) {  
	            content.add(lineContent);  
	            //System.out.println(lineContent);  
	        }  
	          
	        fileInputStream.close();  
	        inputStreamReader.close();  
	        reader.close();  
	    } catch (FileNotFoundException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }
	    //将每行字符与传入id对比
	    for(String query:content){
	    	if(query.split(" ")[0].equals(id)) return true;
	    }
	    return false;
	}
	/**
	 * 
	 * @param directory
	 * @return 文件夹创建是否成功
	 */
	//创建以周为类别的文件夹
	public static boolean createDirectory(String directory){  
	    boolean result = false;  
	    File file = new File(directory);  
	    if(!file.exists()){  
	        result = file.mkdirs();  
	    }  
	      
	    return result;  
	}
	/**
	 * 
	 * @param Directory
	 * @param filePath
	 * @param inputStream
	 * @return 保存文件是否成功
	 */
    //保存邮件附件
	public static boolean saveFile(String Directory,String filePath,InputStream inputStream){  
	    boolean result = false;  
	    File file = new File(Directory+"\\"+filePath);  
	    if(!file.exists()){  
	        try {  
	            result = file.createNewFile();
	            FileOutputStream fileOutputStream = new FileOutputStream(file);
	            int n = 1024;
	            int len =0;
	            byte buffer[] = new byte[n];
	            while((len=inputStream.read(buffer))>0){
	            	fileOutputStream.write(buffer,0,len);
	            } 
	            fileOutputStream.close();
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }  
	    }  
	      
	    return result;  
	}  

	
	
	
	
	public static void main(String[] args) throws Exception {
		
		
		//输入周数
		System.out.println("请输入周数----------------->");
		Scanner kb = new Scanner(System.in);
		int week = kb.nextInt();
		kb.close();
	   if(week<=0){
		   System.out.println("数据输入错误");
	       System.exit(0);
	   }
	   
	   
	   //读入文件，例如week1.dat
	   
	   System.out.println("正在打开记录文件");
	   try{
	      File file = new File("week"+week+".dat");
	       if(file.exists()){
		       System.out.println("记录文件已存在！");
		       FileInputStream fileInputStream = new FileInputStream(file);
		       InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
		       BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		       String lineContent = null;
		       //打印已提交作业成功人员列表
		       System.out.println("以下为已提交成功人员\n");
		       while((lineContent=bufferedReader.readLine())!=null){
		    	   System.out.println(lineContent+"\n");
		       }
		       bufferedReader.close();
		       inputStreamReader.close();
		       fileInputStream.close();
	       }else{
		       //创建文件与文件夹
		   
		       System.out.println("新建文件week"+week+".dat");
		       try {  
	                file.createNewFile();  
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }
	       }
	   }catch (Exception e){
		   System.out.println("读取文件出错");
	   }
		   //文件夹实例week1作业
		   createDirectory("week"+week+"作业");
		   System.out.println("创建文件夹week"+week+"作业");
		   
		
		// 登录邮件服务器，收取邮件
		String host =  "imap.hdu.edu.cn";//邮箱服务器地址
		String username = "15031715";//邮箱用户名
		String password = "Xiao100441;";//邮箱密码
		Properties props = new Properties();//创建空Properties
		Session session = Session.getDefaultInstance(props,null);
		
		Store store = session.getStore("imap");
		store.connect(host, username, password);
		
		 // Get folder
		  Folder folder = store.getFolder("INBOX");
		  folder.open(Folder.READ_ONLY);
		 
		  // Get directory
		  System.out.println("你的邮箱收件箱一共有"+folder.getMessageCount()+"封邮件\n");
		  System.out.println("正在进行查找匹配------------>");
		  
		  Message message[] = folder.getMessages();//将其存放在数组里
		  
		  //使用正则进行对邮件主题的匹配
		  //匹配样式  java高阶第(1)周作业+(15031715)+(肖剑)
		  Pattern p = Pattern.compile("java高阶第"+week+"周作业\\+(\\d{8})\\+([\u4e00-\u9fa5]+)");
		  //邮件计数
		  int mailCount = 0;
		  for(Message msg:message){
			  //System.out.println(msg.getSubject());
			  Matcher m = p.matcher(msg.getSubject());
			  String Sid = "";
			  String name = "";
			  if(m.find()){
				  System.out.println("匹配成功\n");
				  System.out.println(m.group(0));
				  Sid = m.group(1);
				  name = m.group(2);
			  }else{
				  continue;
			  }
			  if(!find(week, Sid)){
				  Multipart mp = (Multipart)msg.getContent();
				  int count = mp.getCount();
				  for(int i=0;i<count;i++){
					  
				      BodyPart bodypart = mp.getBodyPart(i);
				      if(bodypart.getFileName()==null) continue;
				      //保存文件
				      saveFile("week"+week+"作业", "java高阶第"+week+"周作业"+Sid+name+MimeUtility.decodeText(bodypart.getFileName()), 
				    		  bodypart.getInputStream());
			          System.out.println("文件"+MimeUtility.decodeText(bodypart.getFileName())+"已保存\n");
			          //向week1.dat中添加记录
			          add(week,Sid,name);
			          System.out.println("记录："+Sid+" "+name+"已添加");
			          mailCount++;
			      }
			  }
		  }
         System.out.println("此次一共收取了"+mailCount+"封邮件");       
			          // Close connection
			          folder.close(false);
			          store.close();
	}

}
