package app.dividends.application.service;

import java.math.BigDecimal;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import app.dividends.application.ports.input.IMarketDataService;
import app.dividends.domain.model.YahooResponse;

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
            
            if(response.getChart() != null) {
            	return new BigDecimal(response.getChart().getResult().get(0).getMeta().getRegularMarketPrice());
            }
            return BigDecimal.ZERO;
            
        } catch (Exception e) {
        	return BigDecimal.ZERO;
        }
    }

	@Override
	@Cacheable(value = "exchangeRates", key = "#currency")
	public BigDecimal getExchangeRate(String currency) {
		if(currency.equals("EUR")) {return BigDecimal.ONE;}
		String symbol = currency + "EUR=X";
		
		try {
			YahooResponse response = restClient.get()
		            .uri(URL, symbol)
		            .header("User-Agent", "Mozilla/5.0")
		            .retrieve()
		            .body(YahooResponse.class);
			if(response.getChart() != null) {
				return new BigDecimal(response.getChart().getResult().get(0).getMeta().getRegularMarketPrice());
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
        	return BigDecimal.ZERO;
        }
	}

}
