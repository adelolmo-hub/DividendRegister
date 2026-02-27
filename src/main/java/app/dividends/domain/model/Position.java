package app.dividends.domain.model;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Position {

	String ticker;
	int quantity;
	BigDecimal averageCost;
	BigDecimal currentValue;
	BigDecimal totalValue;
	double profit;
	List<Dividend> dividendsRecieved;
	
	public Position(String ticker, int quantity, BigDecimal averageCost, List<Dividend> dividendsRecieved) {
		super();
		this.ticker = ticker;
		this.quantity = quantity;
		this.averageCost = averageCost;
		this.dividendsRecieved = dividendsRecieved;
	}
	
	
	
}
