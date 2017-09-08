package backtesting.backtesting;

public enum OrderStatus {
	FILLED("FILLED"),PARTIALLY_FILLED("PARTIALLY_FILLED"),CANCELLED("CANCELLED"),
	INVALID("INVALID"),UNSPECIFIED("UNSPECIFIED"),FAILED("FAILED");
	
	private String s;
	
	OrderStatus(String s) {
		this.s = s;
	}
	
	@Override
	public String toString() {
		return this.s;
	}
}