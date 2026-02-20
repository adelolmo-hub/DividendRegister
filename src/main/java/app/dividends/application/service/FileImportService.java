package app.dividends.application.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.NoSuchAlgorithmException;
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

import app.dividends.application.ports.input.FileImportUseCase;
import app.dividends.domain.model.Dividend;
import app.dividends.domain.model.ImportReport;
import app.dividends.domain.model.ImportReport.LineError;
import app.dividends.domain.model.Order;
import app.dividends.domain.model.Transaction;
import app.dividends.domain.service.DataExtractor;
import app.dividends.domain.service.DataExtractor.ExtractionResult;
import app.dividends.infrastructure.persistence.TransactionRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileImportService implements FileImportUseCase{

	@Autowired
	private TransactionRepository repo;
	
	private DataExtractor dataExtractor = new DataExtractor();
	
	@Override
	public ImportReport importFile(MultipartFile file, String format) {
		int totalProcessedLines = 0;
		int failedLines = 0;
		int currentLine = 0;
		List<LineError> errors = new LinkedList<>();
		try(Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))){
			CSVParser parser = new CSVParserBuilder()
					.withSeparator(',')
					.build();
			CSVReader csvReader = new CSVReaderBuilder(reader)
					.withSkipLines(1)
					.withCSVParser(parser)
					.build();
			List<String[]> lines = csvReader.readAll();
			
			Transaction transaction = null;
			for(String[] line : lines) {
				try {
					if(line[2].matches("^.*Total.*") || line[1].matches("^.*Total.*") || line[1].equals("Header")) {
						continue;
					}
					switch(line[0]) {
					case "Dividendos":
						transaction = extractDividendValues(line);
						break;
					case "Operaciones":
						transaction = extractOrderValues(line);
						break;
					}
						
					if(!repo.existsByExternalId(transaction.getExternalId())) {
						totalProcessedLines++;
						repo.save(transaction);
					}
				}catch(Exception e) {
					failedLines++;
					errors.add(new LineError(currentLine, String.join(", ", line), e.getMessage()));
				}
				currentLine++;
			}	
		}catch (Exception e) {
			log.error(e.getMessage());
		}
		return new ImportReport(totalProcessedLines, failedLines, errors);	
	}
	
	public Dividend extractDividendValues(String[] line) throws NoSuchAlgorithmException {
		ExtractionResult result = dataExtractor.extractDataFromCsv(line[4], line[2]);
		Dividend dividend = null;
		if(result != null) {
			dividend = new Dividend();
			dividend.setTicker(result.ticker());
			dividend.setCurrency(line[2]);
			dividend.setQuantity(Double.parseDouble(line[5]));
			dividend.setDate(Date.valueOf(line[3]));
			
			if(result.price() != null){
				dividend.setPrice(Double.parseDouble(result.price()));
			}
			String hashId = dividend.calculateId();
			dividend.setExternalId(hashId);
		}
		return dividend;
	}
	
	public Order extractOrderValues(String[] line) {
		Order order = new Order();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
		try {
			order.setTicker(line[5]);
			order.setCurrency(line[4]);
			order.setQuantity(Double.parseDouble(line[7]));
			order.setDate(sdf.parse(line[6]));
			order.setPrice(Double.parseDouble(line[8]));
			
			String hashId = order.calculateId();
			order.setExternalId(hashId);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return order;
	}
}
