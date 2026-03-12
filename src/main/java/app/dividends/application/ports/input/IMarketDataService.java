package app.dividends.application.ports.input;

import java.math.BigDecimal;

public interface IMarketDataService {

	public BigDecimal getCurrentAssetValue(String ticker);
	public BigDecimal getExchangeRate(String currency);
}
