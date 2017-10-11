package test_no_use;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class NoUse_BBG_AMB {

	public static void main(String[] args) {
		int count = 1;  
		
		try {
			String readPath = "D:\\stock data\\A share\\A share.csv";
			String writeRootPath = "D:\\stock data\\A share\\historical data\\";
			
			FileWriter fw = new FileWriter("D:\\test.csv");
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(readPath);
			fw.close();
			
			String line = "";
			 
			int fixLine = 8;
			/*
			 * 数据格式如下，一共8行（包括最后一行的空格）
			 * 	300706 CH Equity							
				Date	25/9/2017	26/9/2017	27/9/2017	28/9/2017	29/9/2017	9/10/2017	10/10/2017
				PX_OPEN	#N/A N/A	11.96	15.8	17.38	19.12	21.03	23.13
				PX_HIGH	#N/A N/A	14.36	15.8	17.38	19.12	21.03	23.13
				PX_LOW	#N/A N/A	11.96	15.8	17.38	19.12	21.03	23.13
				PX_LAST	9.97	14.36	15.8	17.38	19.12	21.03	23.13
				PX_VOLUME	#N/A N/A	5700	2000	2000	4500	5200	6600
							
			 */
			
			ArrayList<String> dateArr = new ArrayList<String> ();
			ArrayList<String> openArr = new ArrayList<String> ();
			ArrayList<String> highArr = new ArrayList<String> ();
			ArrayList<String> lowArr = new ArrayList<String> ();
			ArrayList<String> closeArr = new ArrayList<String> ();
			ArrayList<String> volArr =  new ArrayList<String> ();
			SimpleDateFormat sdf0 = new SimpleDateFormat ("dd/MM/yyyy");
			SimpleDateFormat sdf1 = new SimpleDateFormat ("yyyyMMdd");
			
			while((line = bf.readLine()) != null) {
				int innerLine = Math.floorMod(count, fixLine);
				ArrayList<String> lineArr = new ArrayList<String>(Arrays.asList(line.split(",")));
				
				if(count >= 937 && count <= 944	) { //特殊情况
					count++;
					continue;
				}
				if(count >= 3121 && count <= 3128	) { //特殊情况
					count++;
					continue;
				}
				if(count >= 3625 && count <= 3632	) { //特殊情况
					count++;
					continue;
				}
				if(count >= 4321 && count <= 4328	) { //特殊情况
					count++;
					continue;
				}
					
					
				
				switch(innerLine) {
				case 1:
					// initializing
					String stock = lineArr.get(0);
					stock = tdxStockName(stock);
					fw = new FileWriter(writeRootPath + stock + ".csv");
					
					dateArr = new ArrayList<String> ();
					openArr = new ArrayList<String> ();
					highArr = new ArrayList<String> ();
					lowArr = new ArrayList<String> ();
					closeArr = new ArrayList<String> ();
					volArr = new ArrayList<String> ();
					break;
				case 2:
					ArrayList<String> dateArr0 =  new ArrayList<String> (lineArr.subList(1, lineArr.size()));
					for(int i = 0; i < dateArr0.size(); i++) {
						String date = dateArr0.get(i);
						String date1 = "";
						try {
							date1 = sdf1.format(sdf0.parse(date));  // change to yyyyMMdd
						}catch(Exception e) {
							
						}
						dateArr.add(date1);
					}
					break;
				case 3:
					openArr =  new ArrayList<String> (lineArr.subList(1, lineArr.size()));
					break;
				case 4:
					highArr =  new ArrayList<String> (lineArr.subList(1, lineArr.size()));
					break;
				case 5:
					lowArr =  new ArrayList<String> (lineArr.subList(1, lineArr.size()));
					break;
				case 6:
					closeArr =  new ArrayList<String> (lineArr.subList(1, lineArr.size()));
					break;
				case 7:
					volArr =  new ArrayList<String> (lineArr.subList(1, lineArr.size()));
					break;
				case 0:
					// write
					for(int i = 0; i < openArr.size(); i++) {
						String date = dateArr.get(i);
						String open = openArr.get(i);
						String high = highArr.get(i);
						String low = lowArr.get(i);
						String close = closeArr.get(i);
						String vol = volArr.get(i);
						
						if(open.equals("#N/A N/A")) {
							open = close;
							high = close;
							low = close;
							vol = "0";
						}
						
						fw.write( date + ","
								+  open + ","
								+ high + ","
								+ low + ","
								+ close + ","
								+ vol + "\n");
					}
					fw.close();
					break;
				default:
					break;
				}
				
				count++;
			}
			
			// write
			for(int i = 0; i < openArr.size(); i++) {
				String date = dateArr.get(i);
				String open = openArr.get(i);
				String high = highArr.get(i);
				String low = lowArr.get(i);
				String close = closeArr.get(i);
				String vol = volArr.get(i);
				
				if(open.equals("#N/A N/A")) {
					open = close;
					high = close;
					low = close;
					vol = "0";
				}
				
				fw.write( date + ","
						+  open + ","
						+ high + ","
						+ low + ","
						+ close + ","
						+ vol + "\n");
			}
			fw.close();
			
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("line=" + count);
		}
		
	}
	
	public static String tdxStockName(String stock) {
		String fullName = stock.substring(0,6);
		String subName = stock.substring(0, 2);
		
		if(subName.equals("60"))
			fullName += ".SH";
		if(subName.equals("30") || subName.equals("00") )
			fullName += ".SZ";
		
		fullName += "_tdx";
		
		return fullName;
	}

}
