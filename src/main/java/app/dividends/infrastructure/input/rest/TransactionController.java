package app.dividends.infrastructure.input.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import app.dividends.application.ports.input.IFileImportUseCase;
import app.dividends.application.ports.input.IPortfolioService;
import app.dividends.domain.model.ImportReport;
import app.dividends.domain.model.Position;

@RestController
@RequestMapping("/api")
public class TransactionController {

	@Autowired
	IFileImportUseCase fileImportUseCase;
	
	@Autowired
	IPortfolioService portfolioService;
	
	@PostMapping("/uploadFile")
	public ResponseEntity<ImportReport> uploadFile(
			@RequestParam("file") MultipartFile file,
			@RequestParam("format") String format) {
		ImportReport importReport = fileImportUseCase.importFile(file, format);
		return new ResponseEntity<ImportReport>(importReport, HttpStatus.OK);
	}
	
	@GetMapping("/positions")
	public ResponseEntity<List<Position>> getPositions() {
		return new ResponseEntity<List<Position>>(portfolioService.calculatePortfolio(), HttpStatus.OK);
	}
}
