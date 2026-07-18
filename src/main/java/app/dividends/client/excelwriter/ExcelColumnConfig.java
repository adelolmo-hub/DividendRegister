package app.dividends.client.excelwriter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellReference;

import app.dividends.client.excelwriter.model.ExcelColumns;
import app.dividends.service.models.Dividend;
import app.dividends.service.models.Position;

public class ExcelColumnConfig {

	private static final int DECIMAL_PLACES = 2;
	
	public static final Map<String, ColumnProcessor> COLUMNS = new HashMap<>();
	
	static {
		
		// -----STRINGS-------
		COLUMNS.put(ExcelColumns.TICKER.getNombre(), (cell, pos, styles, year) -> cell.setCellValue(pos.getTicker()));
		
		//------INTEGER----
		COLUMNS.put(ExcelColumns.CANTIDAD.getNombre(), (cell, pos, styles, year) -> cell.setCellValue(pos.getQuantity()));
		
		//------BIGDECIMALS----
		COLUMNS.put(ExcelColumns.PRECIOMEDIO.getNombre(), (cell, pos, styles, year) -> cell.setCellValue(scale(pos.getAverageCost(), RoundingMode.DOWN)));
		COLUMNS.put(ExcelColumns.INGRESOSDIVI.getNombre(), (cell, pos, styles, year) -> cell.setCellValue(scale(pos.getTotalDividends(), RoundingMode.HALF_UP)));
		COLUMNS.put(ExcelColumns.PRECIOACTUAL.getNombre(), (cell, pos, styles, year) -> cell.setCellValue(scale(pos.getCurrentValue(), RoundingMode.HALF_UP)));
		COLUMNS.put(ExcelColumns.COSTE.getNombre(), (cell, pos, styles, year) -> cell.setCellValue(scale(pos.getTotalCostEUR(), RoundingMode.HALF_UP)));
		COLUMNS.put(ExcelColumns.VALORACTUAL.getNombre(), (cell, pos, styles, year) -> cell.setCellValue(scale(pos.getTotalValue(), RoundingMode.HALF_UP)));
		COLUMNS.put(ExcelColumns.RENTABILIDADSINDIVI.getNombre(), (cell, pos, styles, year) -> setProfitStyle(scale(pos.getPercentatgeProfit(), RoundingMode.HALF_UP), styles, cell));
		COLUMNS.put(ExcelColumns.VALORDIVI.getNombre(), (cell, pos, styles, year) -> cell.setCellValue(scale(pos.getTotalValueWitDividends(), RoundingMode.HALF_UP)));
		COLUMNS.put(ExcelColumns.RENTABILIDAD.getNombre(), (cell, pos, styles, year) -> setProfitStyle(scale(pos.getPercentatgeProfitWithDividends(), RoundingMode.HALF_UP), styles, cell));
		
		//-----------MONTHS----
		COLUMNS.put("Enero", (cell, pos, styles, year) -> styleDividendCell(pos, processDividend(pos, YearMonth.of(year, 1)), cell, styles));
		COLUMNS.put("Febrero", (cell, pos, styles, year) -> styleDividendCell(pos, processDividend(pos, YearMonth.of(year, 2)), cell, styles));
		COLUMNS.put("Marzo", (cell, pos, styles, year) -> styleDividendCell(pos, processDividend(pos, YearMonth.of(year, 3)), cell, styles));
		COLUMNS.put("Abril", (cell, pos, styles, year) -> styleDividendCell(pos, processDividend(pos, YearMonth.of(year, 4)), cell, styles));
		COLUMNS.put("Mayo", (cell, pos, styles, year) -> styleDividendCell(pos, processDividend(pos, YearMonth.of(year, 5)), cell, styles));
		COLUMNS.put("Junio", (cell, pos, styles, year) -> styleDividendCell(pos, processDividend(pos, YearMonth.of(year, 6)), cell, styles));
		COLUMNS.put("Julio", (cell, pos, styles, year) -> styleDividendCell(pos, processDividend(pos, YearMonth.of(year, 7)), cell, styles));
		COLUMNS.put("Agosto", (cell, pos, styles, year) -> styleDividendCell(pos, processDividend(pos, YearMonth.of(year, 8)), cell, styles));
		COLUMNS.put("Septiembre", (cell, pos, styles, year) -> styleDividendCell(pos, processDividend(pos, YearMonth.of(year, 9)), cell, styles));
		COLUMNS.put("Octubre", (cell, pos, styles, year) -> styleDividendCell(pos, processDividend(pos, YearMonth.of(year, 10)), cell, styles));
		COLUMNS.put("Noviembre", (cell, pos, styles, year) -> styleDividendCell(pos, processDividend(pos, YearMonth.of(year, 11)), cell, styles));
		COLUMNS.put("Diciembre", (cell, pos, styles, year) -> styleDividendCell(pos, processDividend(pos, YearMonth.of(year, 12)), cell, styles));
		
		//--------------GENERIC--------------
		COLUMNS.put("Total", (cell, pos, styles, year) -> {
			int rowIdx = cell.getRowIndex() + 1;
			String colC = CellReference.convertNumToColString(2);
			String colN = CellReference.convertNumToColString(13);
			
			String formula = String.format("SUM(%s%d:%s%d)", colC, rowIdx, colN, rowIdx);
			cell.setCellFormula(formula);
		});
	}
	
	private static double scale(BigDecimal value, RoundingMode mode) {
		if (value == null) return 0.0;
        return value.setScale(DECIMAL_PLACES, mode).doubleValue();
	}
	
	private static void setProfitStyle(Double value, Map<String, CellStyle> styles, Cell cell) {
		if(value >= 0) {
			cell.setCellStyle(styles.get("percent_positive"));
		}else {
			cell.setCellStyle(styles.get("percent_negative"));
		}
		cell.setCellValue(value);
	}
	
	private static void setCostStyle(Double value, Map<String, CellStyle> styles, Cell cell) {
		if(value >= 0) {
			cell.setCellStyle(styles.get("percent_positive"));
		}else {
			cell.setCellStyle(styles.get("percent_negative"));
		}
		cell.setCellValue(value);
	}
	
	
	private static BigDecimal processDividend(Position pos, YearMonth period) {
	    return pos.findDividendByDate(period)
	              .map(Dividend::getTotalReceivedEUR)
	              .orElse(BigDecimal.ZERO);             
	}
	
	private static void styleDividendCell(Position pos, BigDecimal value, Cell cell, Map<String, CellStyle> styles) {
		if(!value.equals(BigDecimal.ZERO)) {
			cell.setCellStyle(styles.get("dividend_cell"));
			cell.setCellValue(scale(value, RoundingMode.DOWN));
		}
	}
}
