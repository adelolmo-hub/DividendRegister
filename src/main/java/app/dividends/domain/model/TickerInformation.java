package app.dividends.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tickers")
public class TickerInformation {
	@Id
	private Long id; 
	private String ticker;
	private String sufix;
}
