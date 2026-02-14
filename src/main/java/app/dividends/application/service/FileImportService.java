package app.dividends.application.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import app.dividends.application.ports.input.FileImportUseCase;
import app.dividends.domain.model.Transaction;
import app.dividends.infrastructure.persistence.TransactionRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileImportService implements FileImportUseCase{

	@Autowired
	private TransactionRepository repo;
	
	@Override
	public int importFile(MultipartFile file, String format) {
		int count = 0;
		try(Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))){
			
			CSVParser parser = new CSVParserBuilder()
					.withSeparator(';')
					.build();
			
			CSVReader csvReader = new CSVReaderBuilder(reader)
					.withSkipLines(1)
					.withCSVParser(parser)
					.build();
			List<String[]> lines = csvReader.readAll();
			
			
			for(String[] line : lines) {
				Transaction transaction = new Transaction();
				transaction.setTicker(line[0]);
				transaction.setType(line[1]);
				transaction.setQuantity(Double.parseDouble(line[2]));
				transaction.setPrice(Double.parseDouble(line[3]));
				transaction.setFees(Double.parseDouble(line[4]));
				transaction.setDate(Date.valueOf(line[5]));
				
				String hashId = transaction.calculateId();
				transaction.setExternalId(hashId);
				
				if(!repo.existsByExternalId(hashId)) {
					count++;
					repo.save(transaction);
				}
			}
			
			
		}catch (Exception e) {
			log.error(e.getMessage());
		}
		return count;
	}

	
}
