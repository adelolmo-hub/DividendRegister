package app.dividends.client.excelwriter;

import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

import app.dividends.service.models.Position;

@FunctionalInterface
public interface ColumnProcessor {

	void process(Cell cell, Position position, Map<String, CellStyle> styles, int year);
}
