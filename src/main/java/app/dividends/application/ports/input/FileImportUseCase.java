package app.dividends.application.ports.input;

import org.springframework.web.multipart.MultipartFile;

public interface FileImportUseCase {
	int importFile(MultipartFile file, String format);
}
