package app.dividends.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Position {

	private String ticker;
	private int quantity;
	
	private BigDecimal averageCost;
	private BigDecimal currentValue;
	private BigDecimal totalValue;
	private BigDecimal profit;
	private BigDecimal totalProfit;
	private BigDecimal totalProfitWithDividends;
	
	private List<Dividend> dividendsRecieved;
	
	public Position(String ticker, int quantity, BigDecimal averageCost, List<Dividend> dividendsRecieved, BigDecimal currentValue) {
		super();
		this.ticker = ticker;
		this.quantity = quantity;
		this.averageCost = averageCost;
		this.dividendsRecieved = dividendsRecieved;
		this.currentValue = currentValue;
		calculateDerivedFields();
	}
	
	private void calculateDerivedFields() {
		
		BigDecimal totalDividends = dividendsRecieved.stream()
                .map(Dividend::getTotalRecieved)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
		
		if(currentValue.compareTo(BigDecimal.ZERO) != 0 && quantity > 0 /*&& averageCost.compareTo(BigDecimal.ZERO) != 0*/) {
			this.totalValue = currentValue.multiply(BigDecimal.valueOf(quantity));
			
             this.profit = currentValue.subtract(averageCost)
                   .divide(averageCost, 4, RoundingMode.HALF_EVEN)
                   .multiply(new BigDecimal("100")); 
            
            this.totalProfit = currentValue.subtract(averageCost)
                    .multiply(BigDecimal.valueOf(quantity));
            
            this.totalProfitWithDividends = totalProfit.add(totalDividends);
		}
	}
	
	
	
}
