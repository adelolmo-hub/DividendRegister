package app.dividends.domain.model;

import java.util.Date;

public class Dividend {

	String id;
	Date date;
	String ticker;
	double quantityPerStock;
	double quantityTotal;
	double taxOrigin;
	String currency;
}
