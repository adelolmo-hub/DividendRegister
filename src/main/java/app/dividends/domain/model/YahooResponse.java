package app.dividends.domain.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YahooResponse {
    private Chart chart;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Chart {
	    private List<Result> result;
	}
	
	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Result {
	    private Meta meta;
	}
	
	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Meta {
	    private String regularMarketPrice;
	    private String currency;
	}
}
