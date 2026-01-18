package app.dividends.infrastructure.input.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import app.dividends.application.ports.input.FileImportUseCase;

@RestController
@RequestMapping("/api")
public class TransactionController {

	@Autowired
	FileImportUseCase fileImportUseCase;
	
	@PostMapping("/uploadFile")
	public ResponseEntity<String> uploadFile(
			@RequestParam("file") MultipartFile file,
			@RequestParam("format") String format) {
		fileImportUseCase.importFile(file, format);
		return new ResponseEntity<String>("File Imported", HttpStatus.OK);
	}
}
