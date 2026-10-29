package app.dividends.service.models;

import java.util.Date;

import app.dividends.shared.utils.HashUtils;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "transactions", uniqueConstraints = {
		@UniqueConstraint(columnNames = "externalId")
})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class AbstractTransaction implements Transaction{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "externalId", nullable = false, unique = true)
	String externalId;
	
	String ticker;
	String currency;

	@Temporal(TemporalType.TIMESTAMP)
	Date date;
	
	public String calculateId() {
		String raw = String.format("%s|%s", date, ticker);
		return HashUtils.hashSHA256(raw);
	}

	public Long getId() {
		return id;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
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
