package backtesting.backtesting;

public enum OrderType {
	BUY("BUY"),SELL("SELL"),COVER("COVER"),SHORT("SHORT");
	
	private String s;
	
	OrderType(String s) {
		this.s = s;
	}
	
	@Override
	public String toString() {
		return this.s;
	}
}
