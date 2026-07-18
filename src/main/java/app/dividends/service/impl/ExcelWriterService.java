package app.dividends.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Year;
import java.util.HashMap;
import java.util.HexFormat;
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
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.dividends.client.excelwriter.ColumnProcessor;
import app.dividends.client.excelwriter.ExcelColumnConfig;
import app.dividends.client.excelwriter.model.ExcelColumns;
import app.dividends.service.IExcelWriterService;
import app.dividends.service.IPortfolioService;
import app.dividends.service.models.Portfolio;
import app.dividends.service.models.Position;


@Service
public class ExcelWriterService implements IExcelWriterService {

	private final int ROW_OFFSET = 2;
	private final int COLUMN_OFFSET = 1;
	private final int HEADER_OFFSET = 1;
	private final int TABLE_OFFSET = 2;
	
	private final int STARTER_YEAR = 2023;
	private final int CURRENT_YEAR = Year.now().getValue();
	
	private final String[] portfolioHeaderValues = ExcelColumns.getHeaders();
	
	private final String[] dividendHeaderValues = new String[] 
			{"Ticker","Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre", "Total", "Yield"};
	
	private final String FILE_NAME = "portfolio.xlsx";
	
	@Autowired
	private IPortfolioService portfolioService;
	
	@Override
	public String writePortfolio() {
		Workbook workbook = new XSSFWorkbook();

		Portfolio portfolio = portfolioService.calculatePortfolio();
		List<Position> positions = portfolio.getPositions();
		
		Map<String, CellStyle> styles = createStyles(workbook);
		
		Sheet portfolioSheet = workbook.createSheet("Portfolio");
		Sheet dividendSheet = workbook.createSheet("Dividendos");
		
		writeSheet(portfolio, styles, portfolioSheet, portfolioHeaderValues);
		writeDividendSheet(positions, styles, dividendSheet, dividendHeaderValues);
		
		File currDir = new File(".");
		String path = currDir.getAbsolutePath();
		String fileLocation = path.substring(0, path.length() - 1) + FILE_NAME;
		
		try {
			autoSizeColumns(portfolioSheet);
			autoSizeColumns(dividendSheet);
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


	private void writeSheet(Portfolio portfolio, Map<String, CellStyle> styles, Sheet sheet, String[] headerValues) {
		Row header = sheet.createRow(HEADER_OFFSET);
		int rowCount = 0;
		
		List<Position> positions = portfolio.getPositions();
		
		writeHeader(header, headerValues, styles);
		
		for (rowCount = 0; rowCount < positions.size(); rowCount++) {
			Position position = positions.get(rowCount);
			Row row = sheet.createRow(rowCount + ROW_OFFSET);
			for (int j = 0; j < headerValues.length; j++) {
				Cell cell = row.createCell(j + COLUMN_OFFSET);
				setAlternateColorBackground(cell, rowCount, styles);
				ColumnProcessor processor = ExcelColumnConfig.COLUMNS.get(headerValues[j]);
				if(processor != null) {
					processor.process(cell, position, styles, 0);
				}else {
					cell.setCellValue("");
				}
			}
		}
		Row row = sheet.createRow(rowCount + 2);
		for (int i = 0; i < headerValues.length; i++) {
			Cell cell = row.createCell(i + COLUMN_OFFSET);
			setTotalValuesCell(cell, headerValues[i], portfolio, styles);
		}
	}
	



	private void writeDividendSheet(List<Position> positions, Map<String, CellStyle> styles, Sheet sheet, String[] headerValues) {
		int rowCount = 0;
		for(int k = STARTER_YEAR; k <= CURRENT_YEAR; k++) {
			Row row = sheet.createRow(rowCount + ROW_OFFSET); 
			writeHeader(row, headerValues, styles);
			rowCount++;
			for (int i = 0; i < positions.size(); i++) {
				Position position = positions.get(i);
				row = sheet.createRow(rowCount + ROW_OFFSET);
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
			row = sheet.createRow(rowCount + ROW_OFFSET);
			writeFooter(row, styles, headerValues, positions);
			rowCount+= TABLE_OFFSET;
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
			cell.setCellStyle(styles.get("header_cell"));
		}
	}
	
	private void writeFooter(Row footer, Map<String, CellStyle> styles, String[] headerValues, List<Position> positions) {
		//La i empieza en 1 y lenght -1 porque la primera y ultima celda no interesan
		for(int i = 1; i < headerValues.length - 1; i++) {
			Cell cell = footer.createCell(i + COLUMN_OFFSET);
			int rowIndex = cell.getRowIndex() + 1;
			String columnIndex = CellReference.convertNumToColString(cell.getColumnIndex());
			
			String firstColumn = columnIndex + (rowIndex-positions.size());
			String lastColumn = columnIndex + (rowIndex-1); 
			
			String formula = String.format("SUM(%s:%s)", firstColumn, lastColumn);
			cell.setCellFormula(formula);
			cell.setCellStyle(styles.get("dividend_cell"));
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
	
	
	private void setTotalValuesCell(Cell cell, String value, Portfolio portfolio, Map<String, CellStyle> styles) {
		
		switch(value) {
			case "Dividendos Cobrados":
				cell.setCellValue(portfolio.getTotalDividends().doubleValue());
				cell.setCellStyle(styles.get("percent_positive"));
				break;
			case "Valor Actual":
				cell.setCellValue(portfolio.getTotalValue().doubleValue());
				cell.setCellStyle(styles.get("percent_positive"));
				break;
			case "Coste total":
				cell.setCellValue(portfolio.getTotalCost().doubleValue());
				cell.setCellStyle(styles.get("percent_positive"));
				break;
			case "Valor mas Dividendos":
				cell.setCellValue(portfolio.getTotalValueWithDividends().doubleValue());
				cell.setCellStyle(styles.get("percent_positive"));
				break;
			default:
				break;
		}
		
	}
	private Map<String, CellStyle> createStyles(Workbook wb){
		Map<String, CellStyle> mapStyles = new HashMap<>();
	
		XSSFColor pink = new XSSFColor(HexFormat.of().parseHex("ead1dc"));
		XSSFColor blue = new XSSFColor(HexFormat.of().parseHex("3c78d8"));
		XSSFColor yellow = new XSSFColor(HexFormat.of().parseHex("fcba03"));
		
		//Green Style (positives)
		CellStyle posPct = wb.createCellStyle();
		Font greenFont = wb.createFont();
		greenFont.setColor(IndexedColors.GREEN.getIndex());
		posPct.setFont(greenFont);
		posPct.setBorderBottom(BorderStyle.THIN);
		posPct.setBorderTop(BorderStyle.THIN);
		posPct.setBorderLeft(BorderStyle.THIN);
		posPct.setBorderRight(BorderStyle.THIN);
		posPct.setFillForegroundColor(pink);
		posPct.setFillPattern(FillPatternType.SOLID_FOREGROUND);
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
		negPct.setFillForegroundColor(pink);
		negPct.setFillPattern(FillPatternType.SOLID_FOREGROUND);
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
		
		//Header cell
		CellStyle headerCell = wb.createCellStyle();
		Font whiteFont = wb.createFont();
		whiteFont.setColor(IndexedColors.WHITE.getIndex());
		headerCell.setFont(whiteFont);
		headerCell.setFillForegroundColor(blue);
		headerCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerCell.setBorderBottom(BorderStyle.THIN);
		headerCell.setBorderTop(BorderStyle.THIN);
		headerCell.setBorderLeft(BorderStyle.THIN);
		headerCell.setBorderRight(BorderStyle.THIN);
						
		mapStyles.put("header_cell", headerCell);
		
		//Pink cell
		CellStyle pinkCell = wb.createCellStyle();
		pinkCell.setFillForegroundColor(pink);
		pinkCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		pinkCell.setBorderBottom(BorderStyle.THIN);
		pinkCell.setBorderTop(BorderStyle.THIN);
		pinkCell.setBorderLeft(BorderStyle.THIN);
		pinkCell.setBorderRight(BorderStyle.THIN);
						
		mapStyles.put("pink_cell", pinkCell);
		
		return mapStyles;
	}
}
