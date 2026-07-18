package app.dividends.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import app.dividends.api.controller.DTO.ImportReport;
import app.dividends.service.IExcelWriterService;
import app.dividends.service.IFileImportUseCase;
import app.dividends.service.IPortfolioService;
import app.dividends.service.models.Portfolio;

@RestController
@RequestMapping("/api")
public class TransactionController {

	@Autowired
	IFileImportUseCase fileImportUseCase;
	
	@Autowired
	IPortfolioService portfolioService;
	
	@Autowired
	IExcelWriterService excelService;
	
	@PostMapping("/uploadFile")
	public ResponseEntity<ImportReport> uploadFile(
			@RequestParam("file") MultipartFile file,
			@RequestParam("format") String format) {
		ImportReport importReport = fileImportUseCase.importFile(file, format);
		return new ResponseEntity<ImportReport>(importReport, HttpStatus.OK);
	}
	
	@GetMapping("/positions")
	public ResponseEntity<Portfolio> getPositions() {
		return new ResponseEntity<Portfolio>(portfolioService.calculatePortfolio(), HttpStatus.OK);
	}
	
	@GetMapping("/getExcel")
	public String getExcel() {
		return excelService.writePortfolio();
	}
}
