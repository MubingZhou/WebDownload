package utils;

public enum Currency {
	HKD("HKD"),USD("USD"),SGD("SGD"),EUR("EUR"),CNY("CNY"),CNH("CNH");
	
	private String name;
	
	Currency(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
