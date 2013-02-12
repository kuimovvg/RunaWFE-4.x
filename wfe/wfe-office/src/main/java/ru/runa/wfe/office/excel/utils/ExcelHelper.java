package ru.runa.wfe.office.excel.utils;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Preconditions;

public class ExcelHelper {

    public static Sheet getSheet(Workbook workbook, String sheetName, int sheetIndex) {
        if (sheetName != null) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.createSheet(sheetName);
            }
            return sheet;
        } else {
            return workbook.getSheetAt(sheetIndex);

        }
    }

    public static Row getRow(Sheet sheet, int rowIndex, boolean createIfLost) {
        Row row = sheet.getRow(rowIndex);
        if (row == null && createIfLost) {
            row = sheet.createRow(rowIndex);
        }
        return row;
    }

    public static Cell getCell(Row row, int columnIndex, boolean createIfLost) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null && createIfLost) {
            cell = row.createCell(columnIndex);
        }
        return cell;
    }

    public static void setCellValue(Cell cell, Object value) {
        Preconditions.checkNotNull(value);
        if (value instanceof Date) {
            CreationHelper createHelper = cell.getSheet().getWorkbook().getCreationHelper();
            CellStyle cellStyle = cell.getSheet().getWorkbook().createCellStyle();
            cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm"));
            cell.setCellStyle(cellStyle);
            cell.setCellValue((Date) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    public static Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
        case Cell.CELL_TYPE_STRING:
            return cell.getRichStringCellValue().getString();
        case Cell.CELL_TYPE_NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            } else {
                return cell.getNumericCellValue();
            }
        case Cell.CELL_TYPE_BOOLEAN:
            return cell.getBooleanCellValue();
        case Cell.CELL_TYPE_FORMULA:
            return cell.getCellFormula();
        default:
            return cell.getStringCellValue();
        }
    }

    public static boolean isCellEmptyOrNull(Cell cell) {
        if (cell == null) {
            return true;
        }
        return cell.getCellType() == Cell.CELL_TYPE_BLANK;
    }

}
