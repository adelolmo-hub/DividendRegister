package app.dividends.api.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

	private final IFileImportUseCase fileImportUseCase;
	private final IPortfolioService portfolioService;
	private final IExcelWriterService excelService;

	public TransactionController(
			IFileImportUseCase fileImportUseCase,
			IPortfolioService portfolioService,
			IExcelWriterService excelService) {
		this.fileImportUseCase = fileImportUseCase;
		this.portfolioService = portfolioService;
		this.excelService = excelService;
	}
	
	@PostMapping("/uploadFile")
	public ResponseEntity<ImportReport> uploadFile(
			@RequestParam("file") MultipartFile file,
			@RequestParam("format") String format) {
		ImportReport importReport = fileImportUseCase.importFile(file, format);
		return ResponseEntity.ok(importReport);
	}
	
	@GetMapping("/positions")
	public ResponseEntity<Portfolio> getPositions() {
		return ResponseEntity.ok(portfolioService.calculatePortfolio());
	}
	
	@GetMapping("/getExcel")
	public ResponseEntity<byte[]> getExcel() {
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename("portfolio.xlsx").build().toString())
				.body(excelService.writePortfolio());
	}
}
