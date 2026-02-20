package app.dividends.domain.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class ImportReport {

	int totalProcessedLines;
	int failedLines;
	List<LineError> errors;
	
	public record LineError(
			int numberLine,
			String content,
			String errorMessage
			) {}
		
	
}
