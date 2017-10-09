package test_no_use;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.ib.client.Contract;

import org.apache.log4j.LogManager;

import backtesting.backtesting.Order;
import backtesting.backtesting.OrderType;
import backtesting.portfolio.Portfolio;
import cgi.ib.avat.AvatRecordSingleStock;
import cgi.ib.avat.AvatUtils;
import strategy.db_southboundFlowPortfolio.PortfolioScreening;

@SuppressWarnings("unused")
public class Test {

	 //static Logger logger = LogManager.getLogger(Test.class.getName());
	 static Logger logger = Logger.getLogger(Test.class.getName());
	 static int i = 0;
			
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			//JOptionPane.showMessageDialog(null, "标题【出错啦】", "年龄请输入数字", JOptionPane.ERROR_MESSAGE);
			//JOptionPane.showInternalMessageDialog(frame, "information","information", JOptionPane.INFORMATION_MESSAGE); 
			
			Thread t = new Thread(new Runnable(){
				   public void run(){
				       // JOptionPane.showConfirmDialog(null, "运行中!", "test",JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);//你的提示消息   
					   JFrame frame = new JFrame();
					   frame.setLocation(0, 0);
					   Toolkit tk = frame.getToolkit();
					   java.awt.Dimension dm = tk.getScreenSize();  
					   
					   frame.setLocation(0,0);
					   frame.setSize(300, 500);
					   frame.setVisible(true);
					   
					   JOptionPane.showMessageDialog(frame, "标题\n【出错啦】", "年龄请输入数字", JOptionPane.PLAIN_MESSAGE);
					   JOptionPane.showMessageDialog(frame, "标题\n【出错啦】12222", "年龄请输入数字", JOptionPane.PLAIN_MESSAGE);
				        i=1;
				       
				   }
				});
			
			if(true) {
				t.start();
			}
				
			System.out.println("sfsdfsdfsfsd");
			ArrayList<Double> att = new ArrayList<Double>();
			att.add(1.0);
			att.add(3.0);
			att.add(2.0);
			
			Collections.sort(att, Collections.reverseOrder());
			System.out.println(att.get(2));
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
