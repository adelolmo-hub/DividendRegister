package app.dividends.domain.service;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataExtractor{

	private static final String CSV_REGEX = "^([A-Z]+)\\s?[^ (]*\\(.*?\\)(?:.*?%s\\s+([\\d.]+))?";
	public record ExtractionResult(String ticker, String price) {}
	
	public ExtractionResult extractDataFromCsv(String dirtyString, String currency) {
		
		Pattern CSV_PATTERN = Pattern.compile(String.format(CSV_REGEX, currency));
		Matcher matcher = CSV_PATTERN.matcher(dirtyString);
		
		if(matcher.find()) {
			return new ExtractionResult(matcher.group(1), matcher.group(2));
		}
		return null;
	}

}

