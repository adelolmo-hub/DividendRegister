package app.dividends.infrastructure.output;

import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

import app.dividends.domain.model.Position;

@FunctionalInterface
public interface ColumnProcessor {

	void process(Cell cell, Position position, Map<String, CellStyle> styles);
}
