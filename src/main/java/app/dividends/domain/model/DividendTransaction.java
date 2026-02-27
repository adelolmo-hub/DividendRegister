package app.dividends.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("Dividend")
public class DividendTransaction extends Transaction{
	private double taxAmount;
}
