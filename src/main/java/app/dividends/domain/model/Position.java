package app.dividends.domain.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Position {

	String ticker;
	int quantity;
	double averageCost;
	double currentValue;
	double totalValue;
	double profit;
	List<Dividend> dividendsRecieved;
	
	public Position(String ticker, int quantity, double averageCost, List<Dividend> dividendsRecieved) {
		super();
		this.ticker = ticker;
		this.quantity = quantity;
		this.averageCost = averageCost;
		this.dividendsRecieved = dividendsRecieved;
	}
	
	
	
}
