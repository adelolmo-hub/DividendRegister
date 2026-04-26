package app.dividends.application.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
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
import app.dividends.domain.model.Position;
import app.dividends.infrastructure.output.ColumnProcessor;
import app.dividends.infrastructure.output.ExcelColumnConfig;


@Service
public class ExcelWriterService implements IExcelWriterService {

	private final int ROW_OFFSET = 2;
	private final int COLUMN_OFFSET = 1;
	private final int HEADER_OFFSET = 1;
	
	private final int STARTER_YEAR = 2023;
	private final int CURRENT_YEAR = Year.now().getValue();
	
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
		writeDividendSheet(positions, styles, dividendSheet, dividendHeaderValues);
		
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
		
		writeHeader(header, headerValues, styles);
		
		for (int i = 0; i < positions.size(); i++) {
			Position position = positions.get(i);
			Row row = sheet.createRow(i + ROW_OFFSET);
			for (int j = 0; j < headerValues.length; j++) {
				Cell cell = row.createCell(j + COLUMN_OFFSET);
				setAlternateColorBackground(cell, i, styles);
				ColumnProcessor processor = ExcelColumnConfig.COLUMNS.get(headerValues[j]);
				if(processor != null) {
					processor.process(cell, position, styles, 0);
				}else {
					cell.setCellValue("");
				}
				
			}
		}
	}
	
	private void writeDividendSheet(List<Position> positions, Map<String, CellStyle> styles, Sheet sheet, String[] headerValues) {
		int rowCount = 0;
		Row header = sheet.createRow(HEADER_OFFSET);
		writeHeader(header, headerValues, styles);
		for(int k = STARTER_YEAR; k < CURRENT_YEAR; k++) {
			for (int i = 0; i < positions.size(); i++) {
				Position position = positions.get(i);
				Row row = sheet.createRow(rowCount + ROW_OFFSET);
				for (int j = 0; j < headerValues.length; j++) {
					Cell cell = row.createCell(j + COLUMN_OFFSET);
					setAlternateColorBackground(cell, i, styles);
					ColumnProcessor processor = ExcelColumnConfig.COLUMNS.get(headerValues[j]);
					if(processor != null) {
						processor.process(cell, position, styles, k);
					}else {
						cell.setCellValue("");
					}
					
				}
				rowCount++;
			}
			rowCount += ROW_OFFSET; 
		}
	}

	private void setAlternateColorBackground(Cell cell, int i, Map<String, CellStyle> styles) {
		if(i % 2 == 0) {
			cell.setCellStyle(styles.get("light_green_background"));
		}else {
			cell.setCellStyle(styles.get("light_blue_background"));
		}
	}


	private void writeHeader(Row header, String[] headerValues, Map<String, CellStyle> styles) {
		for (int i = 0; i < headerValues.length; i++) {
			Cell cell = header.createCell(i + COLUMN_OFFSET);
			cell.setCellValue(headerValues[i]);
			cell.setCellStyle(styles.get("cell_border"));
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
		Font greenFont = wb.createFont();
		greenFont.setColor(IndexedColors.GREEN.getIndex());
		posPct.setFont(greenFont);
		posPct.setBorderBottom(BorderStyle.THIN);
		posPct.setBorderTop(BorderStyle.THIN);
		posPct.setBorderLeft(BorderStyle.THIN);
		posPct.setBorderRight(BorderStyle.THIN);
		mapStyles.put("percent_positive", posPct);
		
		//Red Style (negatives)
		CellStyle negPct = wb.createCellStyle();
		Font redFont = wb.createFont();
		redFont.setColor(IndexedColors.RED.getIndex());
		negPct.setFont(redFont);
		negPct.setBorderBottom(BorderStyle.THIN);
		negPct.setBorderTop(BorderStyle.THIN);
		negPct.setBorderLeft(BorderStyle.THIN);
		negPct.setBorderRight(BorderStyle.THIN);
		mapStyles.put("percent_negative", negPct);
		
		//Border
		CellStyle border = wb.createCellStyle();
		border.setBorderBottom(BorderStyle.THIN);
		border.setBorderTop(BorderStyle.THIN);
		border.setBorderLeft(BorderStyle.THIN);
		border.setBorderRight(BorderStyle.THIN);
		mapStyles.put("cell_border", border);
		
		
		//Light Green Background
		CellStyle lightGreenBackground = wb.createCellStyle();
		lightGreenBackground.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		lightGreenBackground.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		lightGreenBackground.setBorderBottom(BorderStyle.THIN);
		lightGreenBackground.setBorderTop(BorderStyle.THIN);
		lightGreenBackground.setBorderLeft(BorderStyle.THIN);
		lightGreenBackground.setBorderRight(BorderStyle.THIN);
		
		mapStyles.put("light_green_background", lightGreenBackground);
		
		//Light blue Background
		CellStyle lightBlueBackground = wb.createCellStyle();
		lightBlueBackground.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
		lightBlueBackground.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		lightBlueBackground.setBorderBottom(BorderStyle.THIN);
		lightBlueBackground.setBorderTop(BorderStyle.THIN);
		lightBlueBackground.setBorderLeft(BorderStyle.THIN);
		lightBlueBackground.setBorderRight(BorderStyle.THIN);
		
		mapStyles.put("light_blue_background", lightBlueBackground);
		
		//Dividend cell
		CellStyle dividendCell = wb.createCellStyle();
		dividendCell.setFillForegroundColor(IndexedColors.LIME.getIndex());
		dividendCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		dividendCell.setBorderBottom(BorderStyle.THIN);
		dividendCell.setBorderTop(BorderStyle.THIN);
		dividendCell.setBorderLeft(BorderStyle.THIN);
		dividendCell.setBorderRight(BorderStyle.THIN);
				
		mapStyles.put("dividend_cell", dividendCell);
		
		return mapStyles;
	}
}
