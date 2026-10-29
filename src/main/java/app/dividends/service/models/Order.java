package app.dividends.service.models;

import java.math.BigDecimal;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("Order")
@Getter @Setter
public class Order extends AbstractTransaction{
	private BigDecimal commission;
	int quantity;
	BigDecimal price;
	
	@Override
	public void applyTo(Position position, BigDecimal exchangeRate) {
		if(this.quantity > 0) {
			position.buyOrder(this.quantity, this.price, this.commission, exchangeRate);
		}else {
			position.sellOrder(Math.abs(this.quantity), this.price, this.commission, exchangeRate);
		}
	}
	
	
}
