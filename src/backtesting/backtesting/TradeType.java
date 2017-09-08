package backtesting.backtesting;

/**
 * Trade type
 * @author Murray
 *
 */
public enum TradeType {
	BUY("BUY"),SELL("SELL"),COVER("COVER"),SHORT("SHORT");
	
	private String s;
	
	TradeType(String s) {
		this.s = s;
	}
	
	@Override
	public String toString() {
		return this.s;
	}
}
