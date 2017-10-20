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
			String rootPath = "T:\\Mubing\\stock data\\A share data\\";
			String readPath = rootPath + "northbound stock data.csv";
			String writeRootPath = rootPath + "historical data\\";
			
			FileWriter fw = new FileWriter("D:\\test.csv");
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(readPath);
			fw.close();
			
			String line = "";
			 
			int fixLine = 9;
			/*
			 * 数据格式如下，一共9行（包括最后一行的空格）
			 * 	300706 CH Equity							
				Date	25/9/2017	26/9/2017	27/9/2017	28/9/2017	29/9/2017	9/10/2017	10/10/2017
				PX_OPEN	#N/A N/A	11.96	15.8	17.38	19.12	21.03	23.13
				PX_HIGH	#N/A N/A	14.36	15.8	17.38	19.12	21.03	23.13
				PX_LOW	#N/A N/A	11.96	15.8	17.38	19.12	21.03	23.13
				PX_LAST	9.97	14.36	15.8	17.38	19.12	21.03	23.13
				PX_VOLUME	#N/A N/A	5700	2000	2000	4500	5200	6600
				EQY_SH_OUT	17170.411	17170.411	17170.411	17170.411	17170.411

							
			 */
			
			ArrayList<String> dateArr = new ArrayList<String> ();
			
			ArrayList<String> openArr = new ArrayList<String> ();
			ArrayList<String> highArr = new ArrayList<String> ();
			ArrayList<String> lowArr = new ArrayList<String> ();
			ArrayList<String> closeArr = new ArrayList<String> ();
			ArrayList<String> volArr =  new ArrayList<String> ();
			ArrayList<String> shArr =  new ArrayList<String> ();  // share outstanding
			
			SimpleDateFormat sdf0 = new SimpleDateFormat ("dd/MM/yyyy");
			SimpleDateFormat sdf1 = new SimpleDateFormat ("yyyyMMdd");
			
			while((line = bf.readLine()) != null) {
				int innerLine = Math.floorMod(count, fixLine);
				ArrayList<String> lineArr = new ArrayList<String>(Arrays.asList(line.split(",")));	
				
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
					shArr = new ArrayList<String> ();
					
					break;
				case 2:
					dateArr =  new ArrayList<String> (lineArr.subList(1, lineArr.size()));
					//openArr =  new ArrayList<String> (lineArr.subList(1, lineArr.size()));
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
				case 8:
					shArr =  new ArrayList<String> (lineArr.subList(1, lineArr.size()));
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
						String share = shArr.get(i);
						
						
						if(vol.equals("#N/A N/A")) {
							continue;
						}
						
						Double shareDouble = 0.0;
						try {
							shareDouble = Double.parseDouble(share) * 1000000;
						}catch(Exception e) {
							for(int j = i; j < openArr.size(); j++) {
								try {
									shareDouble = Double.parseDouble(shArr.get(j)) * 1000000;
									break;
								}catch(Exception ee) {
									
								}
							}
						}
						
						String date1 = date;
						try {
							date1 = sdf0.format(sdf0.parse(date));  // change to yyyyMMdd
						}catch(Exception e) {
							//System.out.println("Date formatting failed! i=" + i + " date=" + date1);
						}
						
						fw.write( date1 + ","
								+  open + ","
								+ high + ","
								+ low + ","
								+ close + ","
								+ vol + ","
								+ shareDouble + 
								"\n");
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
				String share = shArr.get(i);
				
				
				if(vol.equals("#N/A N/A")) {
					continue;
				}
				
				Double shareDouble = Double.parseDouble(share) * 1000000;
				String date1 = date;
				try {
					date1 = sdf0.format(sdf0.parse(date));  // change to yyyyMMdd
				}catch(Exception e) {
					System.out.println("Date formatting failed! i=" + i + " date=" + date1);
				}
				
				fw.write( date1 + ","
						+  open + ","
						+ high + ","
						+ low + ","
						+ close + ","
						+ vol + ","
						+ shareDouble + 
						"\n");
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
		
		fullName = "tdx_" + fullName ;
		
		return fullName;
	}

}
