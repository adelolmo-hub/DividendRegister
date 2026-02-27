package app.dividends.application.ports.input;

import org.springframework.web.multipart.MultipartFile;

import app.dividends.domain.model.ImportReport;

public interface IFileImportUseCase {
	ImportReport importFile(MultipartFile file, String format);
}
