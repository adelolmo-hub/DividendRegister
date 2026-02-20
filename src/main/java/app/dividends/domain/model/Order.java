package app.dividends.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("Order")
public class Order extends Transaction{
	private double commission;
}
