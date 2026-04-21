package app.dividends.application.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
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
		
		Map<String, CellStyle> styles = createStyles(workbook);
		
		Sheet sheet = workbook.createSheet("Portfolio");
		Row header = sheet.createRow(HEADER_OFFSET);
		
		writeHeader(header);
		
		List<Position> positions = portfolio.getPositions();
		for (int i = 0; i < positions.size(); i++) {
			Position position = positions.get(i);
			Row row = sheet.createRow(i + ROW_OFFSET);
			for (int j = 0; j < headerValues.length; j++) {
				Cell cell = row.createCell(j + COLUMN_OFFSET);
				try {
					getCellValue(cell, position, headerValues[j], styles);
				}catch (Exception e) {
					cell.setCellValue("null");
				}
			}
		}
		
		File currDir = new File(".");
		String path = currDir.getAbsolutePath();
		String fileLocation = path.substring(0, path.length() - 1) + FILE_NAME;
		
		try {
			autoSizeColumns(sheet);
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
	
	private void getCellValue(Cell cell, Position position, String value, Map<String, CellStyle> styles) {
		String cellValue = "";
		switch(value) {
		case "Ticker":
			cell.setCellValue(position.getTicker());
			break;
		case "Cantidad":
			cell.setCellValue(position.getQuantity());
			break;
		case "Precio Medio":
			cell.setCellValue(position.getAverageCost().doubleValue());
			break;
		case "Dividendos":
			cell.setCellValue(position.getTotalDividends().doubleValue());
			break;
		case "Valor Actual":
			cell.setCellValue(position.getCurrentValue().doubleValue());
			break;
		case "Valor + Dividendos":
			cell.setCellValue(position.getTotalProfitWithDividends().doubleValue());
			break;
		case "Rentabilidad":
			cell.setCellValue(position.getProfit().doubleValue()/100);
			if(position.getProfit().compareTo(BigDecimal.ZERO) >= 0) {
				cell.setCellStyle(styles.get("percent_positive"));
			}else {
				cell.setCellStyle(styles.get("percent_negative"));
			}
			break;
		case "Rentabilidad + Dividendos":
			cell.setCellValue(position.getTotalProfitWithDividends().doubleValue());
			break;
		}
	}
	
	private void autoSizeColumns(Sheet sheet) {
		for (int i = 0; i < headerValues.length; i++) {
	        int columnIndex = i + COLUMN_OFFSET;
	        sheet.autoSizeColumn(columnIndex);

	        int currentWidth = sheet.getColumnWidth(columnIndex);
	       
	        int padding = 256 * 3; 
	        
	        sheet.setColumnWidth(columnIndex, currentWidth + padding);
	    }
	}
	
	private Map<String, CellStyle> createStyles(Workbook wb){
		Map<String, CellStyle> mapStyles = new HashMap<>();
		
		//Green Style (positives)
		CellStyle posPct = wb.createCellStyle();
		DataFormat dtFormat = wb.createDataFormat();
		posPct.setDataFormat(dtFormat.getFormat("0.00%"));
		Font greenFont = wb.createFont();
		greenFont.setColor(IndexedColors.GREEN.getIndex());
		posPct.setFont(greenFont);
		mapStyles.put("percent_positive", posPct);
		
		//Red Style (negatives)
		CellStyle negPct = wb.createCellStyle();
		negPct.setDataFormat(dtFormat.getFormat("0.00%"));
		Font redFont = wb.createFont();
		redFont.setColor(IndexedColors.RED.getIndex());
		negPct.setFont(redFont);
		mapStyles.put("percent_negative", negPct);
		
		return mapStyles;
	}
}
