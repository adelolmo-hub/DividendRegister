package app.dividends.infrastructure.output;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellReference;

import app.dividends.domain.model.Dividend;
import app.dividends.domain.model.Position;

public class ExcelColumnConfig {

	private static final int DECIMAL_PLACES = 2;
	
	public static final Map<String, ColumnProcessor> COLUMNS = new HashMap<>();
	
	static {
		//TODO rellenar esta funcion tostonazo
		
		// -----STRINGS-------
		COLUMNS.put("Ticker", (cell, pos, styles) -> cell.setCellValue(pos.getTicker()));
		
		//------INTEGER----
		COLUMNS.put("Cantidad", (cell, pos, styles) -> cell.setCellValue(pos.getQuantity()));
		
		//------BIGDECIMALS----
		COLUMNS.put("Precio Medio", (cell, pos, styles) -> cell.setCellValue(scale(pos.getAverageCost(), RoundingMode.DOWN)));
		COLUMNS.put("Dividendos", (cell, pos, styles) -> cell.setCellValue(scale(pos.getTotalDividends(), RoundingMode.HALF_UP)));
		COLUMNS.put("Precio Actual", (cell, pos, styles) -> cell.setCellValue(scale(pos.getCurrentValue(), RoundingMode.HALF_UP)));
		COLUMNS.put("Valor Actual Total", (cell, pos, styles) -> cell.setCellValue(scale(pos.getTotalValue(), RoundingMode.HALF_UP)));
		COLUMNS.put("Rentabilidad", (cell, pos, styles) -> setStyle(scale(pos.getProfit(), RoundingMode.HALF_UP), styles, cell));
		COLUMNS.put("Valor + Dividendos", (cell, pos, styles) -> setStyle(scale(pos.getTotalProfitWithDividends(), RoundingMode.HALF_UP), styles, cell));
		
		//-----------MONTHS----
		COLUMNS.put("Enero", (cell, pos, styles) -> cell.setCellValue(scale(processDividend(pos, YearMonth.of(2025, 1)), RoundingMode.DOWN)));
		COLUMNS.put("Febrero", (cell, pos, styles) -> cell.setCellValue(scale(processDividend(pos, YearMonth.of(2025, 2)), RoundingMode.DOWN)));
		COLUMNS.put("Marzo", (cell, pos, styles) -> cell.setCellValue(scale(processDividend(pos, YearMonth.of(2025, 3)), RoundingMode.DOWN)));
		COLUMNS.put("Abril", (cell, pos, styles) -> cell.setCellValue(scale(processDividend(pos, YearMonth.of(2025, 4)), RoundingMode.DOWN)));
		COLUMNS.put("Mayo", (cell, pos, styles) -> cell.setCellValue(scale(processDividend(pos, YearMonth.of(2025, 5)), RoundingMode.DOWN)));
		COLUMNS.put("Junio", (cell, pos, styles) -> cell.setCellValue(scale(processDividend(pos, YearMonth.of(2025, 6)), RoundingMode.DOWN)));
		COLUMNS.put("Julio", (cell, pos, styles) -> cell.setCellValue(scale(processDividend(pos, YearMonth.of(2025, 7)), RoundingMode.DOWN)));
		COLUMNS.put("Agosto", (cell, pos, styles) -> cell.setCellValue(scale(processDividend(pos, YearMonth.of(2025, 8)), RoundingMode.DOWN)));
		COLUMNS.put("September", (cell, pos, styles) -> cell.setCellValue(scale(processDividend(pos, YearMonth.of(2025, 9)), RoundingMode.DOWN)));
		COLUMNS.put("Octubre", (cell, pos, styles) -> cell.setCellValue(scale(processDividend(pos, YearMonth.of(2025, 10)), RoundingMode.DOWN)));
		COLUMNS.put("Noviembre", (cell, pos, styles) -> cell.setCellValue(scale(processDividend(pos, YearMonth.of(2025, 11)), RoundingMode.DOWN)));
		COLUMNS.put("Diciembre", (cell, pos, styles) -> cell.setCellValue(scale(processDividend(pos, YearMonth.of(2025, 12)), RoundingMode.DOWN)));
		
		//--------------GENERIC--------------
		COLUMNS.put("Total", (cell, pos, styles) -> {
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
	
	private static void setStyle(Double value, Map<String, CellStyle> styles, Cell cell) {
		if(value >= 0) {
			cell.setCellStyle(styles.get("percent_positive"));
		}else {
			cell.setCellStyle(styles.get("percent_negative"));
		}
		cell.setCellValue(value);
	}
	
	private static BigDecimal processDividend(Position pos, YearMonth period) {
	    return pos.findDividendByDate(period)
	              .map(Dividend::getTotalRecievedInEur)
	              .orElse(BigDecimal.ZERO);             
	}
}
