package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class SendEmail {
	  // 定义发件人、收件人、SMTP服务器、用户名、密码、主题、内容等
	  private String displayName;
	  private String to;
	  private String from;
	  private String smtpServer;
	  private String username;
	  private String password;
	  private String subject;
	  private String content;
	  private boolean ifAuth; // 服务器是否要身份认证
	  private String filename = "";
	  private Vector file = new Vector(); // 用于保存发送附件的文件名的集合
	  private String contentType = "text/html";
	  private String charset = "utf-8";
	  public void addFile(String filename) {
	    file.add(filename);
	  }
	  public String getContentType() {
	    return contentType;
	  }
	  public void setContentType(String contentType) {
	    this.contentType = contentType;
	  }
	  public String getCharset() {
	    return charset;
	  }
	  public void setCharset(String charset) {
	    this.charset = charset;
	  }
	  /**
	   * 设置SMTP服务器地址
	   */
	  public void setSmtpServer(String smtpServer) {
	    this.smtpServer = smtpServer;
	  }
	  /**
	   * 设置发件人的地址
	   */
	  public void setFrom(String from) {
	    this.from = from;
	  }
	  /**
	   * 设置显示的名称
	   */
	  public void setDisplayName(String displayName) {
	    this.displayName = displayName;
	  }
	  /**
	   * 设置服务器是否需要身份认证
	   */
	  public void setIfAuth(boolean ifAuth) {
	    this.ifAuth = ifAuth;
	  }
	  /**
	   * 设置E-mail用户名
	   */
	  public void setUserName(String username) {
	    this.username = username;
	  }
	  /**
	   * 设置E-mail密码
	   */
	  public void setPassword(String password) {
	    this.password = password;
	  }
	  /**
	   * 设置接收者
	   */
	  public void setTo(String to) {
	    this.to = to;
	  }
	  /**
	   * 设置主题
	   */
	  public void setSubject(String subject) {
	    this.subject = subject;
	  }
	  /**
	   * 设置主体内容
	   */
	  public void setContent(String content) {
	    this.content = content;
	  }
	  public SendEmail() {
	  }
	  private int port = 25;
	  public int getPort() {
	    return port;
	  }
	  public void setPort(int port) {
	    this.port = port;
	  }
	  /**
	   * 发送邮件
	   * 
	   * @throws IOException
	   * @throws FileNotFoundException
	   */
	  public boolean send() throws FileNotFoundException, IOException {
	    HashMap<String, String> map = new HashMap<String, String>();
	    map.put("state", "success");
	    String message = "邮件发送成功！";
	    Session session = null;
	    Properties props = System.getProperties();
	    props.put("mail.smtp.host", smtpServer);
	    props.put("mail.smtp.port", port);
	    try {
	      props.put("mail.smtp.auth", "false");
	      session = Session.getDefaultInstance(props, null);
	      session.setDebug(false);
	      Transport trans = null;
	      Message msg = new MimeMessage(session);
	      try {
	        Address from_address = new InternetAddress(from, displayName);
	        msg.setFrom(from_address);
	      } catch (java.io.UnsupportedEncodingException e) {
	        e.printStackTrace();
	      }
	      InternetAddress[] address = { new InternetAddress(to) };
	      msg.setRecipients(Message.RecipientType.TO, address);
	      msg.setSubject(subject);
	      Multipart mp = new MimeMultipart();
	      MimeBodyPart mbp = new MimeBodyPart();
	      mbp.setContent(content.toString(), getContentType() + ";charset=" + getCharset());
	      mp.addBodyPart(mbp);
	      if (!file.isEmpty()) {// 有附件
	        Enumeration efile = file.elements();
	        while (efile.hasMoreElements()) {
	          mbp = new MimeBodyPart();
	          filename = efile.nextElement().toString(); // 选择出每一个附件名
	          FileDataSource fds = new FileDataSource(filename); // 得到数据源
	          mbp.setDataHandler(new DataHandler(fds)); // 得到附件本身并至入BodyPart
	          mbp.setFileName(MimeUtility.encodeText(fds.getName(), getCharset(),"B")); // 得到文件名同样至入BodyPart
	          mp.addBodyPart(mbp);
	        }
	        file.removeAllElements();
	      }
	      msg.setContent(mp); // Multipart加入到信件
	      msg.setSentDate(new Date()); // 设置信件头的发送日期
	      // 发送信件
	      msg.saveChanges();
	      File f = new File("d:/test.eml");
	      msg.writeTo(new FileOutputStream(f));
	    } catch (AuthenticationFailedException e) {
	      map.put("state", "failed");
	      message = "邮件发送失败！错误原因： " + "身份验证错误!";
	      e.printStackTrace();
	      return false;
	    } catch (MessagingException e) {
	      message = "邮件发送失败！错误原因： " + e.getMessage();
	      map.put("state", "failed");
	      e.printStackTrace();
	      Exception ex = null;
	      if ((ex = e.getNextException()) != null) {
	        System.out.println(ex.toString());
	        ex.printStackTrace();
	      }
	      return false;
	    }
	    // System.out.println(" 提示信息:"+message);
	    map.put("message", message);
	    return true;
	  }
	  public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		  SendEmail o = new SendEmail();
	    o.setSmtpServer("localhost");
	    o.setFrom("zhoumubing@chinastock.com.hk");
	    o.setDisplayName("TOM");
	    o.setTo("zhoumubing@chinastock.com.hk");
	    o.setSubject("Test Subject");
	    o.setContent("Test Content");
	    o.setCharset("GBK");
	    //o.addFile("e:/读我.txt");
	    o.send();
	    System.out.println("here111");
	    Process p = Runtime.getRuntime().exec("cmd /C start msimn.exe /eml:d:/test.eml");
	    
	    
	  }
}
