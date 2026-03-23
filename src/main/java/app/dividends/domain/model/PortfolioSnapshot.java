package app.dividends.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "portfolio_snapshots")
public class PortfolioSnapshot {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	private String ticker;
	private int quantity;
	
	private BigDecimal averageCost;
	private BigDecimal currentValue;
	private BigDecimal currentValueEUR;
	private BigDecimal totalValue;
	private BigDecimal profit; 
	private BigDecimal totalProfit;
	private BigDecimal totalProfitWithDividends;
	
	private LocalDateTime lastUpdate;
}
