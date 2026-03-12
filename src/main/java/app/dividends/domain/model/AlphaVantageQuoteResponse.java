package app.dividends.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AlphaVantageQuoteResponse {
 @JsonProperty("Global Quote")
 private GlobalQuote globalQuote;
 
}
