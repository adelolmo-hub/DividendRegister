package app.dividends.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("Order")
@Getter @Setter
public class Order extends Transaction{
	private BigDecimal commission;
	int quantity;
}
