package app.dividends.application.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import app.dividends.application.ports.input.FileImportUseCase;

@Service
public class FileImportService implements FileImportUseCase{

	@Override
	public void importFile(MultipartFile file, String format) {
		// TODO Auto-generated method stub
		
	}

	
}
