package app.dividends.client.marketdata;

import java.math.BigDecimal;

public interface IMarketDataService {

	public BigDecimal getCurrentAssetValue(String ticker);
	public BigDecimal getExchangeRate(String currency);
}
