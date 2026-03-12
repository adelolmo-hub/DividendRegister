package app.dividends.domain.model;


import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Dividend {

	Date date;
	String currency;
	BigDecimal quantityPerAsset;
	BigDecimal totalRecieved;
	
}
