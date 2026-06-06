package app.dividends.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonPropertyOrder({"ticker", "quantity", "currentValue", "averageCost","totalCost", "totalValue", "profit", "totalProfit", "totalProfitWithDividends", "dividendsRecieved"})
public class Position {

	private String ticker;
	private int quantity = 0;
	
	private BigDecimal averageCost = BigDecimal.ZERO;
	private BigDecimal averageCostEUR = BigDecimal.ZERO;
	private BigDecimal totalCost = BigDecimal.ZERO;
	private BigDecimal totalCostEUR = BigDecimal.ZERO;
	private BigDecimal currentValue = BigDecimal.ZERO;
	private BigDecimal currentValueEUR = BigDecimal.ZERO;
	
	private BigDecimal realizedGains = BigDecimal.ZERO;
	
	private List<Dividend> dividendsRecieved;
	
	
	
	public Position(String ticker, BigDecimal currentValue, BigDecimal currentValueEUR) {
		super();
		this.ticker = ticker;
		this.currentValue = currentValue;
		this.currentValueEUR = currentValueEUR;
		this.dividendsRecieved = new ArrayList<Dividend>();
	}
	
	public Position(String ticker, int quantity, BigDecimal averageCost, BigDecimal totalCost, BigDecimal currentValue,
			BigDecimal currentValueEUR, BigDecimal realizedGains, List<Dividend> dividendsRecieved) {
		super();
		this.ticker = ticker;
		this.quantity = quantity;
		this.averageCost = averageCost;
		this.totalCost = totalCost;
		this.currentValue = currentValue;
		this.currentValueEUR = currentValueEUR;
		this.realizedGains = realizedGains;
		this.dividendsRecieved = dividendsRecieved;
	}
	
	public Optional<Dividend> findDividendByDate(YearMonth yearMonth) {
		return dividendsRecieved.stream()
	            .filter(d -> d.isOcurredAt(yearMonth))
	            .findFirst();
	}

	public void addDividend(Dividend d) {
		dividendsRecieved.add(d);
		
	}

	public void buyOrder(int quantity, BigDecimal price, BigDecimal commission, BigDecimal exchangeRate) {
		
		
		this.quantity += quantity;
		this.totalCost = totalCost.add(price.multiply(BigDecimal.valueOf(quantity)));
		this.totalCost = totalCost.add(commission.abs());
		this.totalCostEUR = totalCost.multiply(exchangeRate);
		
		this.averageCost = totalCost.divide(BigDecimal.valueOf(this.quantity), 8, RoundingMode.HALF_EVEN);
		this.averageCostEUR = averageCost.multiply(exchangeRate);
	}

	public void sellOrder(int quantity, BigDecimal price, BigDecimal commission, BigDecimal exchangeRate) {
		
		if (this.quantity >= quantity) {
			this.quantity -= quantity;
			
			//El coste medio no varia al vender pero el total si. Se actualiza el total en base a las acciones que quedan
			this.totalCost = this.averageCost.multiply(BigDecimal.valueOf(this.quantity));
			this.totalCostEUR = totalCost.multiply(exchangeRate);
			
			BigDecimal commissionEUR = commission.abs().multiply(exchangeRate);
			BigDecimal profitEUR = price.subtract(this.averageCost)
	                .multiply(BigDecimal.valueOf(quantity))
	                .multiply(exchangeRate)
	                .subtract(commissionEUR);
			this.realizedGains = this.realizedGains.add(profitEUR);
		}
	}
	
	public BigDecimal getTotalDividends() {
		return  dividendsRecieved.stream()
                .map(Dividend::getTotalReceivedEUR)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
	}
	
	public BigDecimal getTotalValue() {
		return currentValueEUR.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_EVEN);
	}
	
	public BigDecimal getPercentatgeProfit() {
		if(currentValue.compareTo(BigDecimal.ZERO) != 0 && quantity > 0) {
			
            return currentValue.subtract(averageCost)
                  .divide(averageCost, 4, RoundingMode.HALF_EVEN)
                  .multiply(new BigDecimal("100"))
                  .setScale(2, RoundingMode.HALF_EVEN);
		}
		return BigDecimal.ZERO;
	}
	
	public BigDecimal getPercentatgeProfitWithDividends() {
			if(currentValue.compareTo(BigDecimal.ZERO) != 0 && quantity > 0) {
			
				//Se descuadran cuando no es EUR
				return this.getTotalProfitWithDividends()
		        		.divide(totalCostEUR, 4, RoundingMode.HALF_EVEN)
		                .multiply(new BigDecimal("100"))
		                .setScale(2, RoundingMode.HALF_EVEN);
		}
		return BigDecimal.ZERO;
		  
	}

	public BigDecimal getTotalProfit() {
		if(currentValue.compareTo(BigDecimal.ZERO) != 0 && quantity > 0) {
			
            return currentValue.subtract(averageCost)
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_EVEN);
		}
		return BigDecimal.ZERO;
	}
	
	public BigDecimal getTotalProfitEUR() {
		if(currentValueEUR.compareTo(BigDecimal.ZERO) != 0 && quantity > 0) {
			
            return currentValueEUR.subtract(averageCostEUR)
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_EVEN);
		}
		return BigDecimal.ZERO;
	}
	
	public BigDecimal getTotalProfitWithDividends() {
			if(currentValue.compareTo(BigDecimal.ZERO) != 0 && quantity > 0) {
				return getTotalProfitEUR().add(getTotalDividends())
						.setScale(2, RoundingMode.HALF_EVEN);
			}
			return BigDecimal.ZERO;
	}
	
	public BigDecimal getTotalValueWitDividends() {
		if(currentValue.compareTo(BigDecimal.ZERO) != 0 && quantity > 0) {
			return getTotalValue().add(getTotalDividends());
		}
		return BigDecimal.ZERO;
	}

}
