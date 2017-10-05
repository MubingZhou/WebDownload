package cgi.ib.avat;

import java.util.Calendar;

import backtesting.backtesting.Trade;

public class AvatRecordSingleStock {
	public String stockCode;
	public Double currentPrice;
	//public Double prevClose;
	public Double priceChg;
	public Double avatRatio5D;
	public Double avatRatio20D;
	public String industry;
	public Double industryAvg;
	public Double turnover = 0.0;
	public int rankDiff;
	public int newRank;
	public int oldRank;
	public String isIndexMember="";
	public String isTurnoverOK="";
	public long timeStamp = 0l; // in miliseconds
	
	public AvatRecordSingleStock(long timeStamp, String stockCode, Double currentPrice, Double priceChg, Double avatRatio5D,
			Double avatRatio20D, String industry) {
		super();
		
		this.timeStamp = timeStamp;
		this.stockCode = stockCode;
		this.currentPrice = currentPrice;
		this.priceChg = priceChg;
		this.avatRatio5D = avatRatio5D;
		this.avatRatio20D = avatRatio20D;
		this.industry = industry;
	}
	
	public String toString() {
		return stockCode + "," +  currentPrice + "," + priceChg + "," + avatRatio5D + "," + avatRatio20D + "," + industry
				 + "," + industryAvg + "," + turnover + "," + rankDiff + "," + newRank + "," + oldRank + "," + isIndexMember + "," + isTurnoverOK;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		AvatRecordSingleStock new_t = (AvatRecordSingleStock) super.clone();
		//new_t.stockCode = String.valueOf(stockCode.toCharArray());
		return new_t;
	}
	
	// 按照avatRatio5D从大到小排序
	public static java.util.Comparator<AvatRecordSingleStock> getComparator() {
		return new  java.util.Comparator<AvatRecordSingleStock>() {
			public int compare(AvatRecordSingleStock arg0, AvatRecordSingleStock arg1) {
				return -arg0.avatRatio5D.compareTo(arg1.avatRatio5D);
			}
			
		};
	}
	
}
