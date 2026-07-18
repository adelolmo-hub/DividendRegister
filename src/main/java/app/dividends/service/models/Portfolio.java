package app.dividends.service.models;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Portfolio {

	private List<Position> positions;
	private BigDecimal totalValue;
	private BigDecimal totalValueWithDividends;
	private BigDecimal totalProfit;
	private BigDecimal totalProfitWithDividends;
	private BigDecimal totalDividends;
	private BigDecimal totalCost;
	
	public Portfolio(List<Position> positions) {
		this.positions = positions;
		calculateMetrics();
	}

	private void calculateMetrics() {
		this.totalValue = BigDecimal.ZERO;
	    this.totalDividends = BigDecimal.ZERO;
	    this.totalCost = BigDecimal.ZERO;

	    for (Position position : this.positions) {
	    	boolean qtyNotZero = position.getQuantity() > 0;
	        if (position.getTotalValue() != null && qtyNotZero) {
	            this.totalValue = this.totalValue.add(position.getTotalValue());
	        }
	        if (position.getTotalCost() != null && qtyNotZero) {
	            this.totalCost = totalCost.add(position.getTotalCostEUR());
	            log.info("Ticker: " + position.getTicker() + " | Sumando: " + position.getTotalCostEUR() +" | Total acumulado después: " + this.totalCost);
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
