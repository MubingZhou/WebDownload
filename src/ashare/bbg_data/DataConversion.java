package ashare.bbg_data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class DataConversion {
	private static Logger logger = Logger.getLogger(DataConversion.class);
	
	public static void main(String[] args) {
		// --------------------- stock price conversion -------------------
		//convertToSingleFile_fundamentalDdata("","");
		String hkStockDataInputPath = "Z:\\Mubing\\stock data\\all stock list - bbg.csv";
		String aShareDataInputPath = "Z:\\Mubing\\stock data\\";
		
		String hkStockDataOutputPath = "Z:\\Mubing\\stock data\\stock hist data - bbg\\";
		String aShareDataOutputPath = "Z:\\Mubing\\stock data\\A share data\\historical data\\20171203 all\\";
		
		int market = 2;
		/*
		 * market:
		 * 1 - A share
		 * 2 - HK market
		 */
		
		//convertToSingleFile_priceData(hkStockDataInputPath,hkStockDataOutputPath, market);
		
		
		// -------------------- south bound conversion ---------------
		String sbInputPath = "Z:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\southbound\\combined";
		String sbOutputPath = "Z:\\Mubing\\stock data\\HK CCASS - WEBB SITE\\southbound\\by stock";
		convertSouthboundByStock(sbInputPath, sbOutputPath);
	}
	
	/**
	 * 将BBG输出的在一个文件的file转换成每只股票一个文件
	 * @param inputFileName
	 * @param outputRootPath
	 */
	public static void convertToSingleFile_priceData(String inputFileName, String outputRootPath, int market) {
		/*
		 * market:
		 * 1 - A share
		 * 2 - HK market
		 */
		
		int count = 1;  
		if(!outputRootPath.substring(outputRootPath.length() - 1, outputRootPath.length()).equals("\\"))
			outputRootPath = outputRootPath + "\\";
		
		try {
			// inputFileName = "T:\\Mubing\\stock data\\A share data\\northbound stock data.csv"
			//String rootPath = "T:\\Mubing\\stock data\\A share data\\";
			//String inputFileName = rootPath + "northbound stock data.csv";
			//String outputRootPath = rootPath + "historical data\\";
			
			FileWriter fw = new FileWriter("D:\\test.csv");
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(inputFileName);
			fw.close();
			
			String line = "";
			 
			int fixLine = 9;
			if(market == 2)
				fixLine = 10;
			/*
			 * 如果是A股数据：
			 * 数据格式如下，一共9行（包括最后一行的空格）
			 * 	300706 CH Equity							
				Date	25/9/2017	26/9/2017	27/9/2017	28/9/2017	29/9/2017	9/10/2017	10/10/2017
				PX_OPEN	#N/A N/A	11.96	15.8	17.38	19.12	21.03	23.13
				PX_HIGH	#N/A N/A	14.36	15.8	17.38	19.12	21.03	23.13
				PX_LOW	#N/A N/A	11.96	15.8	17.38	19.12	21.03	23.13
				PX_LAST	9.97	14.36	15.8	17.38	19.12	21.03	23.13
				PX_VOLUME	#N/A N/A	5700	2000	2000	4500	5200	6600
				EQY_SH_OUT	17170.411	17170.411	17170.411	17170.411	17170.411

				如果是港股数据：
				一共10行，700 HK Equity							
				Date	25/9/2017	26/9/2017	27/9/2017	28/9/2017	29/9/2017	9/10/2017	10/10/2017
				PX_OPEN	#N/A N/A	11.96	15.8	17.38	19.12	21.03	23.13
				PX_HIGH	#N/A N/A	14.36	15.8	17.38	19.12	21.03	23.13
				PX_LOW	#N/A N/A	11.96	15.8	17.38	19.12	21.03	23.13
				PX_LAST	9.97	14.36	15.8	17.38	19.12	21.03	23.13
				PX_VOLUME	#N/A N/A	5700	2000	2000	4500	5200	6600
				EQY_SH_OUT	17170.411	17170.411	17170.411	17170.411	17170.411
				Free Float Pct 40	40	40	
							
			 */
			
			ArrayList<String> dateArr = new ArrayList<String> ();
			
			ArrayList<String> openArr = new ArrayList<String> ();
			ArrayList<String> highArr = new ArrayList<String> ();
			ArrayList<String> lowArr = new ArrayList<String> ();
			ArrayList<String> closeArr = new ArrayList<String> ();
			ArrayList<String> volArr =  new ArrayList<String> ();
			ArrayList<String> shArr =  new ArrayList<String> ();  // share outstanding
			ArrayList<String> freeFlowPctArr =  new ArrayList<String> ();   // free float pct
			
			SimpleDateFormat sdf0 = new SimpleDateFormat ("dd/MM/yyyy");
			SimpleDateFormat sdf1 = new SimpleDateFormat ("yyyyMMdd");
			
			while((line = bf.readLine()) != null) {
				int innerLine = Math.floorMod(count, fixLine);
				ArrayList<String> lineArr = new ArrayList<String>(Arrays.asList(line.split(",")));	
				
				switch(innerLine) {
				case 1:
					// initializing
					String stock = lineArr.get(0);
					if(market == 1) {
						stock = tdxStockName(stock);
					}
					if(market == 2) {
						stock = bbgStockNameInAB(stock, market);
					}
					logger.info("stock=" + stock);
					
					fw = new FileWriter(outputRootPath + stock + ".csv");
					
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
				case 9:
					if(market == 2) {
						freeFlowPctArr = new ArrayList<String> (lineArr.subList(1, lineArr.size()));
					}
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
						
						//deal with volume
						if(vol.equals("#N/A N/A")) {
							vol = "0";
						}
						
						// deal with prices
						if(utils.Utils.isDouble(close)) {
							if(!utils.Utils.isDouble(open) || !utils.Utils.isDouble(high) || !utils.Utils.isDouble(low)) {
								if(utils.Utils.isDouble(vol) && Double.parseDouble(vol)==0.0) {
									open=high=low=close;
								}else {
									continue;
								}
							}
						}else
							continue;
						
						// deal with shares
						Double shareDouble = 0.0;
						for(int j = i; j >= 0; j--) {   //如果这个数据有问题，则向前找上一个数据代替
							try {
								shareDouble = Double.parseDouble(shArr.get(j)) * 1000000;
								break;
							}catch(Exception ee) {
								
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
								+ shareDouble);
						
						// HK market
						if(market == 2) {
							Double freeFloatPctDouble = 100.0;
							for(int j = i; j >= 0; j--) {   //如果这个数据有问题，则向前找上一个数据代替
								try {
									freeFloatPctDouble = Double.parseDouble(freeFlowPctArr.get(j)) ;
									break;
								}catch(Exception ee) {
									
								}
							}
							fw.write("," + String.valueOf(freeFloatPctDouble / 100 * shareDouble));
						}
						
						
						fw.write("\n");
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
	
	public static void convertToSingleFile_fundamentalDdata(String inputFileName, String outputRootPath, int market) {
		/*
		 * market:
		 * 1 - A share
		 * 2 - HK market
		 */
		int count = 1;  
		
		try {
			// inputFileName = "T:\\Mubing\\stock data\\A share data\\northbound stock data.csv"
			//String rootPath = "T:\\Mubing\\stock data\\A share data\\";
			//String inputFileName = rootPath + "northbound stock data.csv";
			//String outputRootPath = rootPath + "historical data\\";
			
			FileWriter fw = new FileWriter("D:\\test.csv");
			BufferedReader bf = utils.Utils.readFile_returnBufferedReader(inputFileName);
			fw.close();
			
			String line = "";
			 
			int fixLine = 7;
			/*
			 * 数据格式如下，一共7行（包括最后一行的空格）
			 * 	000001 CH Equity					
				Date	4/1/2000	5/1/2000	6/1/2000	7/1/2000	10/1/2000
				PX_LAST	4.445	4.389	4.564	4.749	4.895
				TRAIL_12M_EPS	#N/A N/A	#N/A N/A	#N/A N/A	#N/A N/A	#N/A N/A
				NET_ASSETS	#N/A N/A	#N/A N/A	#N/A N/A	#N/A N/A	#N/A N/A
				EQY_SH_OUT	5577.711	5577.711	5577.711	5577.711	5577.711
				
			 */
			
			ArrayList<String> dateArr = new ArrayList<String> ();
			ArrayList<String> closeArr = new ArrayList<String> ();
			ArrayList<String> epsArr = new ArrayList<String> ();
			ArrayList<String> netAssetArr = new ArrayList<String> ();
			ArrayList<String> totalSharesArr = new ArrayList<String> ();
			String stock = "";
			
			SimpleDateFormat sdf0 = new SimpleDateFormat ("dd/MM/yyyy");
			SimpleDateFormat sdf1 = new SimpleDateFormat ("yyyyMMdd");
			
			while((line = bf.readLine()) != null) {
				int innerLine = Math.floorMod(count, fixLine);
				ArrayList<String> lineArr = new ArrayList<String>(Arrays.asList(line.split(",")));	
				
				switch(innerLine) {
				case 1:
					// initializing
					stock = bbgStockNameInAB(stock, market);
					fw = new FileWriter(outputRootPath + stock + ".csv");
					
					dateArr = new ArrayList<String> ();
					closeArr = new ArrayList<String> ();
					epsArr = new ArrayList<String> ();
					netAssetArr = new ArrayList<String> ();
					totalSharesArr = new ArrayList<String> ();
					
					break;
				case 2:
					dateArr =  new ArrayList<String> (lineArr.subList(1, lineArr.size()));
					//openArr =  new ArrayList<String> (lineArr.subList(1, lineArr.size()));
					break;
				case 3:
					closeArr =  new ArrayList<String> (lineArr.subList(1, lineArr.size()));
					break;
				case 4:
					epsArr =  new ArrayList<String> (lineArr.subList(1, lineArr.size()));
					break;
				case 5:
					netAssetArr =  new ArrayList<String> (lineArr.subList(1, lineArr.size()));
					break;
				case 6:  // 更新最后一行的数据，然后写入
					totalSharesArr =  new ArrayList<String> (lineArr.subList(1, lineArr.size()));
					
					FileWriter fw_eps = new FileWriter(outputRootPath + stock + "_eps.csv");
					FileWriter fw_pe = new FileWriter(outputRootPath + stock + "_pe.csv");
					FileWriter fw_pb = new FileWriter(outputRootPath + stock + "_pb.csv");
					FileWriter fw_roe = new FileWriter(outputRootPath + stock + "_roe.csv");
					
					for(int i = 0; i < dateArr.size(); i++) {
						String date = dateArr.get(i);
						String eps = epsArr.get(i);
						String netAsset = netAssetArr.get(i);
						String totalShares = totalSharesArr.get(i);
						String close = closeArr.get(i);
						
						
						if(close.equals("#N/A N/A") || !utils.Utils.isDouble(close)) {
							continue;
						}
						
						if(utils.Utils.isDouble(eps)) {
							fw_eps.write(date + "," + eps + "\n");
							fw_pe.write(date + "," + Double.parseDouble(close) / Double.parseDouble(eps) + "\n");
						}
						
						double bps = -1.0;
						if(!totalShares.equals("#N/A N/A") && utils.Utils.isDouble(totalShares) && utils.Utils.isDouble(netAsset)) {
							bps = Double.parseDouble(netAsset) / Double.parseDouble(totalShares);
							double pb = Double.parseDouble(close) / bps;
							fw_pb.write(date + "," + pb + "\n");
							
							if(utils.Utils.isDouble(eps)) {
								double roe = Double.parseDouble(eps) / bps;
								fw_roe.write(date + "," + roe + "\n");
							}
								
						}
						
					}
					fw_eps.close();
					fw_pe.close();
					fw_pb.close();
					fw_roe.close();
					break;
				default:
					break;
				}
				
				count++;
			}
			

			
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("line=" + count);
		}
	}
	
	/**
	 * 目前的southbound数据是以日期为一个文件存储，这个函数转换成
	 * @param inputFilePath
	 * @param outputRootPath
	 */
	public static void convertSouthboundByStock(String inputFilePath, String outputRootPath) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //文件名都是这个格式的日期
			
			ArrayList<Date> fileDateList = new ArrayList<Date>(); 
			File[] fileList = new File(inputFilePath).listFiles();
			for(File f : fileList) {
				String fileName = f.getName();
				Date d = new Date();
				String fileNameDate = fileName.substring(0, fileName.length() - 4);
				try {
					d = sdf.parse(fileNameDate);
				}catch(Exception e) {
					d = null;
				}
				if(d != null) {
					fileDateList.add(d);
				}
			}
			Collections.sort(fileDateList);  //从旧到新排序
			
			Map<String, FileWriter> fwMap = new HashMap<String, FileWriter>();
			int dateC = 0;
			for(Date d : fileDateList) {
				String todayDateStr = sdf.format(d);
				logger.info("file:" + todayDateStr + ".csv");
				
				String filePathName = utils.Utils.addBackSlashToPath(inputFilePath) + todayDateStr + ".csv";
				BufferedReader bf = utils.Utils.readFile_returnBufferedReader(filePathName);
				String line = "";
				int lineC = 0;
				while((line = bf.readLine()) != null) {
					if(lineC == 0) {
						lineC ++;
						continue;
					}
					
					String[] arr = line.split(",");
					String stock = arr[0];
					String holdingStr = arr[2];
					
					FileWriter fw = fwMap.get(stock);
					if(fw == null)
						fw = new FileWriter(utils.Utils.addBackSlashToPath(outputRootPath) + bbgStockNameInAB(stock, 2) + "_southbound" + ".csv");
					fw.write(todayDateStr + "," + holdingStr + "\n");
					
					if(Math.floorMod(dateC, 100) == 0)
						fw.flush();
					
					fwMap.put(stock, fw);
					
				}
				
				dateC ++;
			}
			
			
			// close all file writers
			for(FileWriter fw : fwMap.values()) {
				fw.close();
			}
			
			
		}catch(Exception e) {
			
		}
	}
	
	/**
	 * stock 是bbg形式的，比如002001 CH Equity，转换成tdx_002001.SZ
	 * @param stock
	 * @return
	 */
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
	
	/**
	 * stock 是bbg形式的，比如002001 CH Equity，转换成bbg_002001_CH
	 * 700 HK Equity -> bbg_700_HK
	 * @param stock
	 * @return
	 */
	public static String bbgStockNameInAB(String stock, int market) {
		/*
		 * market
		 * 1 - A share
		 * 2 - HK market
		 */
		if(market == 1) {
			String fullName = stock.substring(0,6);
			
			fullName = "tdx_" + fullName + "_CH";
			
			return fullName;
		}
		
		if(market == 2) {
			String[] arr  =stock.split(" ");
			return "bbg_" + arr[0] + "_HK";
		}
		
		return null;
		
	}
}
