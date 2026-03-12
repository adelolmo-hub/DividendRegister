package app.dividends.application.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import app.dividends.application.ports.input.IMarketDataService;
import app.dividends.domain.model.AlphaVantageQuoteResponse;

@Service
public class MarketDataService implements IMarketDataService{
	
	private final RestTemplate restTemplate;
	
	@Value("${alphavantage.api.key}")
    private String apiKey;

    private static final String URL = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol={symbol}&apikey={apikey}";
    
    public MarketDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
	@Override
	@Cacheable(value = "prices", key = "#ticker")
	public BigDecimal getCurrentAssetValue(String ticker){
		try {
			
            ResponseEntity<AlphaVantageQuoteResponse> response = restTemplate.getForEntity(
                URL, AlphaVantageQuoteResponse.class, ticker, apiKey);
            Thread.sleep(1000);

            if (response.getBody() != null && response.getBody().getGlobalQuote() != null) {
                String priceStr = response.getBody().getGlobalQuote().getPrice();
                return new BigDecimal(priceStr);
            }
            throw new RuntimeException("Precio no encontrado para " + ticker);
            
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

	@Override
	public BigDecimal getExchangeRate(String currency) {
		BigDecimal price;
		
		return null;
	}

}
