package app.dividends.domain.model;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

import app.dividends.domain.model.utils.HashUtils;



public class Transaction {

	String id;
	String ticker;
	String type;
	Double quantity;
	Double price;
	Double fees;
	Date date;
	
	public String calculateId() throws NoSuchAlgorithmException {
		String raw = String.format("/s|/s|/s|/f|/f", date, ticker, type, quantity, price);
		return HashUtils.hashSHA256(raw);
	}
}
