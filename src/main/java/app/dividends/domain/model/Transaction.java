package app.dividends.domain.model;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

import app.dividends.domain.model.utils.HashUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "transactions", uniqueConstraints = {
		@UniqueConstraint(columnNames = "external_id")
})
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "externalId", nullable = false)
	String externalId;
	
	String ticker;
	String type;
	Double quantity;
	Double price;
	Double fees;
	Date date;
	
	public String calculateId() throws NoSuchAlgorithmException {
		String raw = String.format("%s|%s|%s|%f|%f", date, ticker, type, quantity, price);
		return HashUtils.hashSHA256(raw);
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public Double getFees() {
		return fees;
	}

	public void setFees(Double fees) {
		this.fees = fees;
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

	
	
	
}
