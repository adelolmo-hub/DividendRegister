package app.dividends.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("Dividend")
@Getter @Setter
public class DividendTransaction extends Transaction{
	private BigDecimal taxAmount;
	private BigDecimal amountReceived;
}
