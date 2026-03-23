package app.dividends.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonPropertyOrder({"ticker", "quantity", "currentValue", "averageCost", "totalValue", "profit", "totalProfit", "totalProfitWithDividends", "dividendsRecieved"})
public class Position {

	private String ticker;
	private int quantity;
	
	private BigDecimal averageCost;
	private BigDecimal currentValue;
	private BigDecimal currentValueEUR;
	private BigDecimal totalValue;
	private BigDecimal profit; 
	private BigDecimal totalProfit;
	private BigDecimal totalProfitWithDividends;
	private BigDecimal totalDividends;
	
	private List<Dividend> dividendsRecieved;
	
	public Position(String ticker, int quantity, BigDecimal averageCost, List<Dividend> dividendsRecieved, BigDecimal currentValue, BigDecimal currenValueEUR) {
		super();
		this.ticker = ticker;
		this.quantity = quantity;
		this.averageCost = averageCost;
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
		}
	}
	
	
	
}
