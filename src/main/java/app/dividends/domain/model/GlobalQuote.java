package app.dividends.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GlobalQuote {
    @JsonProperty("05. price")
    private String price;
    
    @JsonProperty("10. change percent")
    private String changePercent;
}
