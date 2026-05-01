package app.dividends.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonPropertyOrder({"ticker", "quantity", "currentValue", "averageCost","totalCost", "totalValue", "profit", "totalProfit", "totalProfitWithDividends", "dividendsRecieved"})
public class Position {

	private String ticker;
	private int quantity;
	
	private BigDecimal averageCost;
	private BigDecimal totalCost;
	private BigDecimal currentValue;
	private BigDecimal currentValueEUR;
	private BigDecimal totalValue;
	private BigDecimal totalValueWithDividends;
	private BigDecimal profit; 
	private BigDecimal totalProfit;
	private BigDecimal totalProfitWithDividends;
	private BigDecimal totalDividends;
	private BigDecimal totalProfitWithDividendsPercent;
	
	private List<Dividend> dividendsRecieved;
	
	public Position(String ticker, int quantity, BigDecimal averageCost,BigDecimal totalCost, List<Dividend> dividendsRecieved, BigDecimal currentValue, BigDecimal currenValueEUR) {
		super();
		this.ticker = ticker;
		this.quantity = quantity;
		this.averageCost = averageCost;
		this.totalCost = totalCost;
		this.dividendsRecieved = dividendsRecieved;
		this.currentValue = currentValue;
		this.currentValueEUR = currenValueEUR;
		calculateDerivedFields();
	}
	
	private void calculateDerivedFields() {
		
		 this.totalDividends = dividendsRecieved.stream()
                .map(Dividend::getTotalRecievedInEur)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
		
		if(currentValue.compareTo(BigDecimal.ZERO) != 0 && quantity > 0) {
			this.totalValue = currentValueEUR.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_EVEN);
			
             this.profit = currentValueEUR.subtract(averageCost)
                   .divide(averageCost, 4, RoundingMode.HALF_EVEN)
                   .multiply(new BigDecimal("100"))
                   .setScale(2, RoundingMode.HALF_EVEN); 
            
            this.totalProfit = currentValueEUR.subtract(averageCost)
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_EVEN);
            
            this.totalProfitWithDividends = totalProfit.add(totalDividends)
            		.setScale(2, RoundingMode.HALF_EVEN);
            
            this.totalProfitWithDividendsPercent = totalProfitWithDividends
            		.divide(averageCost, 4, RoundingMode.HALF_EVEN)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_EVEN);
            
            this.totalValueWithDividends = totalValue.add(totalDividends);
		}
	}
	
	public Optional<Dividend> findDividendByDate(YearMonth yearMonth) {
		return dividendsRecieved.stream()
	            .filter(d -> d.isOcurredAt(yearMonth))
	            .findFirst();
	}
	
	
}
