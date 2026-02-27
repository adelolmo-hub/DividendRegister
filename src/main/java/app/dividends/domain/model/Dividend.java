package app.dividends.domain.model;


import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Dividend {

	Date date;
	String currency;
	double quantityPerAsset;
	double totalRecieved;
	
}
