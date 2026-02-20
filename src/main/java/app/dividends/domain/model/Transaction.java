package app.dividends.domain.model;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

import app.dividends.domain.model.utils.HashUtils;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "transactions", uniqueConstraints = {
		@UniqueConstraint(columnNames = "external_id")
})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "externalId", nullable = false)
	String externalId;
	
	String ticker;
	String currency;
	Double quantity;
	Double price;
	Date date;
	
	public String calculateId() throws NoSuchAlgorithmException {
		String raw = String.format("%s|%s|%s|%f", date, ticker, quantity, price);
		return HashUtils.hashSHA256(raw);
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String external_id) {
		this.externalId = external_id;
	}
	
	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	
	
	
}
