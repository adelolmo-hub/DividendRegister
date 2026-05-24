package app.dividends.domain.model;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class Portfolio {

	private List<Position> positions;
	private BigDecimal totalValue;
	private BigDecimal totalValueWithDividends;
	private BigDecimal totalProfit;
	private BigDecimal totalProfitWithDividends;
	private BigDecimal totalDividends;
	
	public Portfolio(List<Position> positions) {
		this.positions = positions;
		calculateMetrics();
	}

	private void calculateMetrics() {
		this.totalValue = BigDecimal.ZERO;
	    BigDecimal totalCost = BigDecimal.ZERO;
	    this.totalDividends = BigDecimal.ZERO;

	    for (Position position : this.positions) {
	    	boolean qtyNotZero = position.getQuantity() > 0;
	        if (position.getTotalValue() != null && qtyNotZero) {
	            this.totalValue = this.totalValue.add(position.getTotalValue());
	        }
	        if (position.getTotalCost() != null && qtyNotZero) {
	            totalCost = totalCost.add(position.getTotalCost());
	        }
	        if (position.getTotalDividends() != null) {
	            this.totalDividends = this.totalDividends.add(position.getTotalDividends());
	        }
	    }

	    this.totalValueWithDividends = this.totalValue.add(this.totalDividends);

	    
	    this.totalProfit = this.totalValue.subtract(totalCost);

	    
	    this.totalProfitWithDividends = this.totalProfit.add(this.totalDividends);
	}
}
