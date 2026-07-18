package app.dividends.service;

import org.springframework.web.multipart.MultipartFile;

import app.dividends.api.controller.DTO.ImportReport;

public interface IFileImportUseCase {
	ImportReport importFile(MultipartFile file, String format);
}
