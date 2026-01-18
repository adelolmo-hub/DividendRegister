package app.dividends.application.ports.input;

import org.springframework.web.multipart.MultipartFile;

public interface FileImportUseCase {
	void importFile(MultipartFile file, String format);
}
