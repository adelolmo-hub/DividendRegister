package app.dividends.client.marketdata.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import app.dividends.client.marketdata.IMarketDataService;
import app.dividends.client.marketdata.model.YahooResponse;
import app.dividends.client.marketdata.model.YahooResponse.Meta;
import app.dividends.client.marketdata.model.YahooResponse.Result;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MarketDataService implements IMarketDataService{
	
	private final RestClient restClient;

    private static final String URL = "https://query1.finance.yahoo.com/v8/finance/chart/{symbol}";
    
    public MarketDataService(RestClient restClient) {
        this.restClient = restClient;
    }
    
	@Override
	@Cacheable(value = "prices", key = "#ticker")
	public BigDecimal getCurrentAssetValue(String ticker){
		try {
            YahooResponse response = restClient.get()
            .uri(URL, ticker)
            .header("User-Agent", "Mozilla/5.0")
            .retrieve()
            .body(YahooResponse.class);
            
            Meta meta = extractMeta(response);
            if (meta == null || meta.getRegularMarketPrice() == null) {
            	return BigDecimal.ZERO;
            }
            if ("GBp".equals(meta.getCurrency())) {
        		return new BigDecimal(meta.getRegularMarketPrice()).movePointLeft(2);
        	}
        	return new BigDecimal(meta.getRegularMarketPrice());
        } catch (Exception e) {
        	log.warn("No se pudo obtener el precio de {}: {}", ticker, e.getMessage());
        	return BigDecimal.ZERO;
        }
    }

	@Override
	@Cacheable(value = "exchangeRates", key = "#currency")
	public BigDecimal getExchangeRate(String currency) {
		if (currency == null || currency.isBlank() || "EUR".equalsIgnoreCase(currency)) {
			return BigDecimal.ONE;
		}
		String symbol = currency + "EUR=X";
		
		try {
			YahooResponse response = restClient.get()
		            .uri(URL, symbol)
		            .header("User-Agent", "Mozilla/5.0")
		            .retrieve()
		            .body(YahooResponse.class);
			Meta meta = extractMeta(response);
			if (meta == null || meta.getRegularMarketPrice() == null) {
				return BigDecimal.ZERO;
			}
			return new BigDecimal(meta.getRegularMarketPrice());
        } catch (Exception e) {
        	log.warn("No se pudo obtener el tipo de cambio de {}: {}", currency, e.getMessage());
        	return BigDecimal.ZERO;
        }
	}

	private Meta extractMeta(YahooResponse response) {
		if (response == null || response.getChart() == null) {
			return null;
		}
		List<Result> results = response.getChart().getResult();
		if (results == null || results.isEmpty() || results.get(0) == null) {
			return null;
		}
		return results.get(0).getMeta();
	}

}
