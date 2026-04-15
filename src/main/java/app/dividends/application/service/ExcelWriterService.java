package app.dividends.application.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.dividends.application.ports.input.IExcelWriterService;
import app.dividends.application.ports.input.IPortfolioService;
import app.dividends.domain.model.PortfolioDTO;
import app.dividends.domain.model.Position;


@Service
public class ExcelWriterService implements IExcelWriterService {

	private final int ROW_OFFSET = 2;
	private final int COLUMN_OFFSET = 1;
	private final int HEADER_OFFSET = 1;
	
	private final String[] headerValues = new String[] 
			{"Ticker","Cantidad","Precio Medio","Dividendos","Valor Actual","Valor + Dividendos","Rentabilidad","Rentabilidad + Dividendos"};
	
	private final String FILE_NAME = "temp.xlsx";
	
	@Autowired
	private IPortfolioService portfolioService;
	
	@Override
	public String writePortfolio() {
		Workbook workbook = new XSSFWorkbook();
		PortfolioDTO portfolio = portfolioService.calculatePortfolio();
		
		Sheet sheet = workbook.createSheet("Portfolio");
		sheet.setColumnWidth(0, 12000);
		Row header = sheet.createRow(HEADER_OFFSET);
		
		writeHeader(header);
		
		List<Position> positions = portfolio.getPositions();
		for (int i = 0; i < positions.size(); i++) {
			Position position = positions.get(i);
			Row row = sheet.createRow(i + ROW_OFFSET);
			for (int j = 0; j < headerValues.length; j++) {
				Cell cell = row.createCell(j + COLUMN_OFFSET);
				try {
					cell.setCellValue(getCellValue(position, headerValues[j]));
				}catch (Exception e) {
					cell.setCellValue("null");
				}
			}
		}
		
		File currDir = new File(".");
		String path = currDir.getAbsolutePath();
		String fileLocation = path.substring(0, path.length() - 1) + FILE_NAME;
		
		try {
			FileOutputStream outputStream = new FileOutputStream(fileLocation);
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();
		} catch (FileNotFoundException e) {
			return e.getMessage();
		} catch (IOException e) {
			return e.getMessage();
		}
		return "Done!";
	}

	private void writeHeader(Row header) {
		for (int i = 0; i < headerValues.length; i++) {
			Cell cell = header.createCell(i + COLUMN_OFFSET);
			cell.setCellValue(headerValues[i]);
		}
	}
	
	private String getCellValue(Position position, String value) {
		String cellValue = "";
		switch(value) {
		case "Ticker":
			cellValue = position.getTicker();
			break;
		case "Cantidad":
			cellValue = position.getQuantity() + "";
			break;
		case "Precio Medio":
			cellValue = position.getAverageCost().toString();
			break;
		case "Dividendos":
			cellValue = position.getTotalDividends().toString();
			break;
		case "Valor Actual":
			cellValue = position.getCurrentValue().toString();
			break;
		case "Valor + Dividendos":
			cellValue = position.getTotalProfitWithDividends().toString();
			break;
		case "Rentabilidad":
			cellValue = position.getProfit().toString();
			break;
		case "Rentabilidad + Dividendos":
			cellValue = position.getTotalProfitWithDividends().toString();
			break;
		}
		return cellValue;
	}
	
}
