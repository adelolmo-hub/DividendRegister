package app.dividends.application.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import app.dividends.infrastructure.output.ColumnProcessor;
import app.dividends.infrastructure.output.ExcelColumnConfig;


@Service
public class ExcelWriterService implements IExcelWriterService {

	private final int ROW_OFFSET = 2;
	private final int COLUMN_OFFSET = 1;
	private final int HEADER_OFFSET = 1;
	
	private final String[] portfolioHeaderValues = new String[] 
			{"Ticker","Cantidad","Precio Medio","Precio Actual","Dividendos","Valor Actual Total","Valor + Dividendos","Rentabilidad"};
	
	private final String[] dividendHeaderValues = new String[] 
			{"Ticker","Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre", "Total", "Yield"};
	
	private final String FILE_NAME = "portfolio.xlsx";
	
	@Autowired
	private IPortfolioService portfolioService;
	
	@Override
	public String writePortfolio() {
		Workbook workbook = new XSSFWorkbook();

		List<Position> positions = portfolioService.calculatePortfolio().getPositions();
		
		Map<String, CellStyle> styles = createStyles(workbook);
		
		Sheet portfolioSheet = workbook.createSheet("Portfolio");
		Sheet dividendSheet = workbook.createSheet("Dividendos");
		
		writeSheet(positions, styles, portfolioSheet, portfolioHeaderValues);
		writeSheet(positions, styles, dividendSheet, dividendHeaderValues);
		
		File currDir = new File(".");
		String path = currDir.getAbsolutePath();
		String fileLocation = path.substring(0, path.length() - 1) + FILE_NAME;
		
		try {
			autoSizeColumns(portfolioSheet);
			autoSizeColumns(portfolioSheet);
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


	private void writeSheet(List<Position> positions, Map<String, CellStyle> styles, Sheet sheet, String[] headerValues) {
		Row header = sheet.createRow(HEADER_OFFSET);
		
		writeHeader(header, headerValues);
		
		for (int i = 0; i < positions.size(); i++) {
			Position position = positions.get(i);
			Row row = sheet.createRow(i + ROW_OFFSET);
			for (int j = 0; j < headerValues.length; j++) {
				Cell cell = row.createCell(j + COLUMN_OFFSET);
				ColumnProcessor processor = ExcelColumnConfig.COLUMNS.get(headerValues[j]);
				if(processor != null) {
					processor.process(cell, position, styles);
				}else {
					cell.setCellValue("");
				}
			}
		}
	}

	private void writeHeader(Row header, String[] headerValues) {
		for (int i = 0; i < headerValues.length; i++) {
			Cell cell = header.createCell(i + COLUMN_OFFSET);
			cell.setCellValue(headerValues[i]);
		}
	}
	
	private void autoSizeColumns(Sheet sheet) {
		for (int i = 0; i < portfolioHeaderValues.length; i++) {
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
		Font greenFont = wb.createFont();
		greenFont.setColor(IndexedColors.GREEN.getIndex());
		posPct.setFont(greenFont);
		mapStyles.put("percent_positive", posPct);
		
		//Red Style (negatives)
		CellStyle negPct = wb.createCellStyle();
		Font redFont = wb.createFont();
		redFont.setColor(IndexedColors.RED.getIndex());
		negPct.setFont(redFont);
		mapStyles.put("percent_negative", negPct);
		
		return mapStyles;
	}
}
