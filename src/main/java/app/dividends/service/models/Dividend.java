package app.dividends.service.models;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Dividend {

	private final Date date;
    private final String currency;
    private final BigDecimal amountPerAsset;
    private final BigDecimal totalReceivedEUR;
	
	public boolean isOcurredAt(YearMonth targetDate) {
		if (date == null || targetDate == null) {
			return false;
		}
		// java.sql.Date.toInstant() lanza UnsupportedOperationException
		return YearMonth.from(Instant.ofEpochMilli(date.getTime())
				.atZone(ZoneId.systemDefault())
				.toLocalDate()).equals(targetDate);
	}
}
