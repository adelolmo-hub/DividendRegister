package app.dividends.domain.model;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data@AllArgsConstructor
public class PortfolioDTO {

	private List<Position> positions;
	private BigDecimal totalValue;
	private BigDecimal totalValueWithDividends;
	private BigDecimal totalProfit;
	private BigDecimal totalProfitWithDividends;
	private BigDecimal totalDividends;
}
