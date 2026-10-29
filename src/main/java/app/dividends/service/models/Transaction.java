package app.dividends.service.models;

import java.math.BigDecimal;

public interface Transaction {

	void applyTo(Position position, BigDecimal exchangeRate);
}
