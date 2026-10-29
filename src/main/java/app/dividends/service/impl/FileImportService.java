package app.dividends.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import app.dividends.api.controller.DTO.ImportReport;
import app.dividends.api.controller.DTO.ImportReport.LineError;
import app.dividends.client.TransactionRepository;
import app.dividends.service.IFileImportUseCase;
import app.dividends.service.impl.DataExtractor.ExtractionResult;
import app.dividends.service.models.AbstractTransaction;
import app.dividends.service.models.DividendTransaction;
import app.dividends.service.models.Order;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileImportService implements IFileImportUseCase{

	@Autowired
	private TransactionRepository repo;
	
	private DataExtractor dataExtractor = new DataExtractor();
	
	@Override
	public ImportReport importFile(MultipartFile file, String format) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("El fichero de importación está vacío o no se ha enviado");
		}

		int totalProcessedLines = 0;
		int failedLines = 0;
		int currentLine = 0;
		List<LineError> errors = new LinkedList<>();
		char separator = resolveSeparator(format);

		try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
				CSVReader csvReader = buildCsvReader(reader, separator)) {
			List<String[]> lines = csvReader.readAll();
			
			for (String[] line : lines) {
				try {
					currentLine++;
					if (shouldSkipLine(line)) {
						continue;
					}
					AbstractTransaction transaction = mapLine(line);
					if (transaction == null) {
						continue;
					}

					if (transaction.getId() != null) {
						repo.save(transaction);
						totalProcessedLines++;
						continue;
					}
					
					if (transaction.getExternalId() != null && !repo.existsByExternalId(transaction.getExternalId())) {
						totalProcessedLines++;
						repo.save(transaction);
					}
				} catch (Exception e) {
					failedLines++;
					errors.add(new LineError(currentLine, String.join(", ", line), e.getMessage()));
				}
			}	
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			log.error("No se pudo leer el fichero de importación", e);
			throw new IllegalArgumentException("No se pudo leer el fichero CSV: " + e.getMessage(), e);
		}
		return new ImportReport(totalProcessedLines, failedLines, errors);	
	}

	private CSVReader buildCsvReader(Reader reader, char separator) {
		CSVParser parser = new CSVParserBuilder()
				.withSeparator(separator)
				.build();
		return new CSVReaderBuilder(reader)
				.withCSVParser(parser)
				.build();
	}

	private char resolveSeparator(String format) {
		if (format == null || format.isBlank() || "csv".equalsIgnoreCase(format) || "degiro".equalsIgnoreCase(format)) {
			return ',';
		}
		if ("semicolon".equalsIgnoreCase(format) || ";".equals(format)) {
			return ';';
		}
		throw new IllegalArgumentException("Formato no soportado: " + format);
	}

	private boolean shouldSkipLine(String[] line) {
		if (line == null || line.length < 6) {
			return false;
		}
		return line[2].matches("^.*Total.*") || line[1].matches("^.*Total.*") || line[1].equals("Header") || line[3].equals("Fórex");
	}

	private AbstractTransaction mapLine(String[] line) {
		if (line == null || line.length == 0) {
			throw new IllegalArgumentException("Línea vacía");
		}
		return switch (line[0]) {
			case "Dividendos" -> extractDividendValues(line);
			case "Operaciones" -> extractOrderValues(line);
			case "Retención de impuestos" -> extractDividendTaxes(line);
			default -> null;
		};
	}

	private AbstractTransaction extractDividendTaxes(String[] line) {
		requireColumns(line, 6);
		ExtractionResult result = dataExtractor.extractDataFromCsv(line[4], line[2]);
		if (result == null || result.ticker() == null) {
			throw new IllegalArgumentException("No se pudo extraer el ticker de la retención");
		}
		Date date = Date.valueOf(line[3]);
		DividendTransaction dividendTx = repo.findDividendByTickerAndDate(result.ticker(), date)
				.orElseThrow(() -> new IllegalArgumentException(
						"No existe el dividendo de " + result.ticker() + " en " + date + " para aplicar la retención"));
		dividendTx.setTaxAmount(new BigDecimal(line[5]));
		return dividendTx;
	}

	public DividendTransaction extractDividendValues(String[] line) {
		requireColumns(line, 6);
		ExtractionResult result = dataExtractor.extractDataFromCsv(line[4], line[2]);
		if (result == null || result.ticker() == null) {
			throw new IllegalArgumentException("No se pudo extraer el ticker de la línea de dividendos");
		}
		DividendTransaction dividend = new DividendTransaction();
		dividend.setTicker(result.ticker());
		dividend.setCurrency(line[2]);
		dividend.setAmountReceived(new BigDecimal(line[5]));
		dividend.setDate(Date.valueOf(line[3]));
		
		if (result.price() != null) {
			dividend.setAmountReceivedByAsset(new BigDecimal(result.price()));
		}
		dividend.setExternalId(dividend.calculateId());
		return dividend;
	}
	
	public Order extractOrderValues(String[] line) {
		requireColumns(line, 12);
		Order order = new Order();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
		try {
			order.setTicker(line[5]);
			order.setCurrency(line[4]);
			order.setQuantity(Integer.parseInt(line[7]));
			order.setDate(sdf.parse(line[6]));
			order.setPrice(new BigDecimal(line[8]));
			order.setCommission(new BigDecimal(line[11]));
			order.setExternalId(order.calculateId());
			return order;
		} catch (ParseException e) {
			throw new IllegalArgumentException("Fecha de operación inválida: " + line[6], e);
		}
	}

	private void requireColumns(String[] line, int minLength) {
		if (line.length < minLength) {
			throw new IllegalArgumentException("La línea no tiene columnas suficientes (mínimo " + minLength + ")");
		}
	}
}
