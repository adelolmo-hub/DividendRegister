package app.dividends.domain.model;

import java.math.BigDecimal;

public interface Transaction {

	void applyTo(Position position, BigDecimal exchangeRate);
}
