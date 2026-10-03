package app.dividends.service.models;

import java.math.BigDecimal;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("Dividend")
@Getter @Setter
public class DividendTransaction extends AbstractTransaction{
	private BigDecimal taxAmount;
	private BigDecimal amountReceived;
	private BigDecimal amountReceivedByAsset;
	
	@Override
	public void applyTo(Position position, BigDecimal exchangeRate) {
		 BigDecimal received = this.getAmountReceived() != null ? this.getAmountReceived() : BigDecimal.ZERO;
		 BigDecimal rate = exchangeRate != null ? exchangeRate : BigDecimal.ONE;
		 Dividend d = new Dividend(
		    this.getDate(),
		    this.getCurrency(),
		    this.getAmountReceivedByAsset(),
		    received.multiply(rate)
		    );
		 position.addDividend(d);
	}
	
}
