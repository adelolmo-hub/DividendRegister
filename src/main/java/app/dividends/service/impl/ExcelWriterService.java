package app.dividends.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
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
	private final int ROW_OFFSET_PROFIT_VALUES = 3;
	private final int ROW_OFFSET_SOLD_POSITIONS = 3;
	private final int COLUMN_OFFSET = 1;
	private final int HEADER_OFFSET = 1;
	private final int TABLE_OFFSET = 2;
	
	private final int STARTER_YEAR = 2023;
	private final int CURRENT_YEAR = Year.now().getValue();
	
	private final String[] portfolioHeaderValues = ExcelColumns.getHeaders();
	
	private final String[] soldHeaderValues = new String[]
			{"Ticker", "Ganancias realizadas", "Dividendos Cobrados", "Beneficio total"};
	
	private final String[] dividendHeaderValues = new String[] 
			{"Ticker","Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre", "Total", "Yield"};
	
	@Autowired
	private IPortfolioService portfolioService;
	
	@Override
	public byte[] writePortfolio() {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			Portfolio portfolio = portfolioService.calculatePortfolio();
			List<Position> positions = portfolio.getPositions();
			
			Map<String, CellStyle> styles = createStyles(workbook);
			
			Sheet portfolioSheet = workbook.createSheet("Portfolio");
			Sheet dividendSheet = workbook.createSheet("Dividendos");
			
			writeSheet(portfolio, styles, portfolioSheet, portfolioHeaderValues);
			writeDividendSheet(positions, styles, dividendSheet, dividendHeaderValues);
			
			int soldStartCol = COLUMN_OFFSET + portfolioHeaderValues.length + ROW_OFFSET_SOLD_POSITIONS;
			autoSizeColumns(portfolioSheet, COLUMN_OFFSET, portfolioHeaderValues.length);
			autoSizeColumns(portfolioSheet, soldStartCol, soldHeaderValues.length);
			autoSizeColumns(dividendSheet, COLUMN_OFFSET, dividendHeaderValues.length);
			workbook.write(outputStream);
			return outputStream.toByteArray();
		} catch (IOException e) {
			throw new IllegalStateException("No se pudo generar el Excel: " + e.getMessage(), e);
		}
	}


	private void writeSheet(Portfolio portfolio, Map<String, CellStyle> styles, Sheet sheet, String[] headerValues) {
		Row header = sheet.createRow(HEADER_OFFSET);
		int rowCount = 0;
		int rowCountSoldPositions = 0;
		int soldStartCol = COLUMN_OFFSET + headerValues.length + ROW_OFFSET_SOLD_POSITIONS;
		
		List<Position> positions = portfolio.getPositions();
		List<Position> soldPositions = new ArrayList<>();
		
		writeHeader(header, headerValues, styles, COLUMN_OFFSET);
		writeHeader(header, soldHeaderValues, styles, soldStartCol);
		
		for (int i = 0; i < positions.size(); i++) {
			Position position = positions.get(i);
			
			if (isSoldPosition(position)) {
				Row row = getOrCreateRow(sheet, rowCountSoldPositions + ROW_OFFSET);
				writePositionRow(row, position, soldHeaderValues, soldStartCol, rowCountSoldPositions, styles);
				soldPositions.add(position);
				rowCountSoldPositions++;
				continue;
			}
			
			if (position.getQuantity() == 0) {
				continue;
			}
			
			Row row = getOrCreateRow(sheet, rowCount + ROW_OFFSET);
			writePositionRow(row, position, headerValues, COLUMN_OFFSET, rowCount, styles);
			rowCount++;
		}
		
		Row row = getOrCreateRow(sheet, rowCount + ROW_OFFSET);
		for (int i = 0; i < headerValues.length; i++) {
			Cell cell = row.createCell(i + COLUMN_OFFSET);
			setTotalValuesCell(cell, headerValues[i], portfolio, styles);
		}
		rowCount++;
		
		if (rowCountSoldPositions > 0) {
			Row soldTotalRow = getOrCreateRow(sheet, rowCountSoldPositions + ROW_OFFSET);
			for (int i = 0; i < soldHeaderValues.length; i++) {
				Cell cell = soldTotalRow.createCell(i + soldStartCol);
				setSoldTotalValuesCell(cell, soldHeaderValues[i], soldPositions, styles);
			}
		}
		
		writeProfitValuesFooter(portfolio, styles, sheet, rowCount);
	}
	
	private boolean isSoldPosition(Position position) {
		return position.getQuantity() == 0
				&& position.getRealizedGains() != null
				&& position.getRealizedGains().compareTo(BigDecimal.ZERO) != 0;
	}
	
	private void writePositionRow(Row row, Position position, String[] headerValues, int startColumn,
			int rowIndexForColor, Map<String, CellStyle> styles) {
		for (int j = 0; j < headerValues.length; j++) {
			Cell cell = row.createCell(j + startColumn);
			setAlternateColorBackground(cell, rowIndexForColor, styles);
			ColumnProcessor processor = ExcelColumnConfig.COLUMNS.get(headerValues[j]);
			if (processor != null) {
				processor.process(cell, position, styles, 0);
			} else {
				cell.setCellValue("");
			}
		}
	}


	private void writeProfitValuesFooter(Portfolio portfolio, Map<String, CellStyle> styles, Sheet sheet,
			int rowCount) {
		
		int currentRow = rowCount + ROW_OFFSET_PROFIT_VALUES;
		
		//Normal Profit
		Row row = getOrCreateRow(sheet, currentRow);
		Cell textCell = row.createCell(COLUMN_OFFSET);
		Cell cellProfit = row.createCell(COLUMN_OFFSET + 1);
		textCell.setCellValue("Beneficio sin Divs");
		textCell.setCellStyle(styles.get("light_green_background"));
		setTotalValuesCell(cellProfit, "profit", portfolio, styles);
		
		//ProfitWithDivs
		currentRow++;
		row = getOrCreateRow(sheet, currentRow);
		textCell = row.createCell(COLUMN_OFFSET);
		cellProfit = row.createCell(COLUMN_OFFSET + 1);
		textCell.setCellValue("Beneficio bruto");
		textCell.setCellStyle(styles.get("light_green_background"));
		setTotalValuesCell(cellProfit, "profitwithdivs", portfolio, styles);
	}
	
	private void writeDividendSheet(List<Position> positions, Map<String, CellStyle> styles, Sheet sheet, String[] headerValues) {
		int rowCount = 0;
		for(int k = STARTER_YEAR; k <= CURRENT_YEAR; k++) {
			Row row = sheet.createRow(rowCount + ROW_OFFSET); 
			writeHeader(row, headerValues, styles, COLUMN_OFFSET);
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


	private void writeHeader(Row header, String[] headerValues, Map<String, CellStyle> styles, int startColumn) {
		for (int i = 0; i < headerValues.length; i++) {
			Cell cell = header.createCell(i + startColumn);
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
	
	private void autoSizeColumns(Sheet sheet, int startColumn, int columnCount) {
		for (int i = 0; i < columnCount; i++) {
	        int columnIndex = i + startColumn;
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
			case "profit":
				cell.setCellValue(portfolio.getTotalProfitPctg().doubleValue());
				cell.setCellStyle(styles.get("percent_positive"));
				break;
			case "profitwithdivs":
					cell.setCellValue(portfolio.getTotalProfitWithDividendsPctg().doubleValue());
					cell.setCellStyle(styles.get("percent_positive"));
					break;
			default:
				break;
		}
		
	}

	private void setSoldTotalValuesCell(Cell cell, String value, List<Position> soldPositions, Map<String, CellStyle> styles) {
		switch (value) {
			case "Ticker":
				cell.setCellValue("Total");
				cell.setCellStyle(styles.get("percent_positive"));
				break;
			case "Ganancias realizadas":
				cell.setCellValue(sumRealizedGains(soldPositions).doubleValue());
				cell.setCellStyle(styles.get("percent_positive"));
				break;
			case "Dividendos Cobrados":
				cell.setCellValue(sumDividends(soldPositions).doubleValue());
				cell.setCellStyle(styles.get("percent_positive"));
				break;
			case "Beneficio total":
				cell.setCellValue(sumTotalProfitWithDividends(soldPositions).doubleValue());
				cell.setCellStyle(styles.get("percent_positive"));
				break;
			default:
				break;
		}
	}

	private BigDecimal sumRealizedGains(List<Position> positions) {
		return positions.stream()
				.map(p -> p.getRealizedGains() != null ? p.getRealizedGains() : BigDecimal.ZERO)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal sumDividends(List<Position> positions) {
		return positions.stream()
				.map(Position::getTotalDividends)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal sumTotalProfitWithDividends(List<Position> positions) {
		return positions.stream()
				.map(Position::getTotalProfitWithDividends)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
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
	
	private Row getOrCreateRow(Sheet sheet, int rowIndex) {
	    Row row = sheet.getRow(rowIndex);
	    if (row == null) {
	        row = sheet.createRow(rowIndex);
	    }
	    return row;
	}
}
