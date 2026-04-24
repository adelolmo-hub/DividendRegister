package app.dividends.domain.model;


import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Dividend {

	Date date;
	String currency;
	BigDecimal quantityPerAsset;
	BigDecimal totalRecievedInEur;
	
	public boolean isOcurredAt(YearMonth targetDate) {	
		return YearMonth.from(date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()).equals(targetDate);
	}
}
