package app.dividends.service.models;


import java.math.BigDecimal;
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
		return YearMonth.from(date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()).equals(targetDate);
	}
}
